package smo;

import java.util.Locale;

public class ConsolePrinter {
    public static boolean SHOW_CALENDAR = true;

    private static String f(double v) { return String.format(Locale.US, "%.4f", v); }

    public static void printStep(StepRecord s) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==================== ");
        sb.append(s.event == null ? "Начальное состояние" : "Шаг " + s.stepNumber);
        sb.append(" | t = ").append(f(s.time)).append(" ====================\n");
        if (s.event != null) sb.append("Событие: ").append(s.event.describe()).append('\n');

        sb.append("Что произошло:\n");
        for (String line : s.log) sb.append("  - ").append(line).append('\n');

        sb.append("Источники:\n");
        for (String st : s.sourceStates) sb.append("  ").append(st).append('\n');

        sb.append("Буфер (ячеек: ").append(s.buffer.length).append("):\n");
        for (int i = 0; i < s.buffer.length; i++) {
            Request r = s.buffer[i];
            sb.append("  [").append(i).append("] ");
            sb.append(r == null ? "пусто" : "заявка " + r + " (создана " + f(r.getTimeCreated()) + ")");
            StringBuilder marks = new StringBuilder();
            if (i == s.bufferPointer) marks.append("УБ ");
            if (i == s.selectPointer) marks.append("УВ ");
            if (!marks.isEmpty()) sb.append("   <- ").append(marks.toString().trim().replace(" ", ", "));
            sb.append('\n');
        }

        sb.append("Приборы:\n");
        for (int i = 0; i < s.deviceStates.length; i++) {
            sb.append("  ").append(s.deviceStates[i]);
            if (i == s.devicePointer) sb.append("   <- УП");
            sb.append('\n');
        }

        sb.append("Указатели: УБ=").append(s.bufferPointer)
          .append("  УВ=").append(s.selectPointer)
          .append("  УП=").append(s.devicePointer).append('\n');

        double pct = s.totalGenerated == 0 ? 0 : 100.0 * s.totalRejected / s.totalGenerated;
        sb.append(String.format(Locale.US, "Всего: создано %d, отказов %d, %% отказа = %.2f%%%n",
                s.totalGenerated, s.totalRejected, pct));

        if (SHOW_CALENDAR) {
            sb.append("Календарь: ");
            if (s.calendar.length == 0) sb.append("пуст");
            for (int i = 0; i < s.calendar.length; i++) {
                if (i > 0) sb.append(" | ");
                sb.append(s.calendar[i]);
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }

    public static void printSummary(SummaryTable t) {
        int nSrc = t.generated().length - 1;
        System.out.println("\n==================== ИТОГОВЫЕ ТАБЛИЦЫ (ОР2) ====================");
        System.out.println("Общее время реализации: " + f(t.tEnd()));
        System.out.println("Всего заявок: " + t.generated()[nSrc] + ", отказов: " + t.rejected()[nSrc]
                + ", обслужено: " + t.processed()[nSrc]);

        System.out.println("\nТаблица 1. Характеристики источников ВС");
        System.out.printf(Locale.US, "%-10s %8s %9s %10s %10s %10s %10s %10s%n",
                "№ источн.", "Заявок", "P отк", "T преб", "T БП", "T обсл", "D БП", "D обсл");
        for (int i = 0; i <= nSrc; i++) {
            String name = i == nSrc ? "ИТОГО" : "И" + i;
            System.out.printf(Locale.US, "%-10s %8d %9.4f %10.4f %10.4f %10.4f %10.4f %10.4f%n",
                    name, t.generated()[i], t.rejectProbability()[i], t.avgSystem()[i], t.avgWait()[i],
                    t.avgService()[i], t.waitVariance()[i], t.serviceVariance()[i]);
        }

        System.out.println("\nТаблица 2. Характеристики приборов ВС");
        System.out.printf(Locale.US, "%-10s %s%n", "№ прибора", "Коэффициент использования");
        for (int i = 0; i < t.deviceLoad().length; i++) {
            System.out.printf(Locale.US, "%-10s %.4f%n", "П" + i, t.deviceLoad()[i]);
        }
        System.out.println("\nT преб = T БП + T обсл (по обслуженным заявкам); T БП - время в буфере; D - дисперсия.");
    }

    public static void printAutoIterations(AutoMode.Result r) {
        System.out.println("\n==================== ПОДБОР ЧИСЛА ЗАЯВОК N ====================");
        System.out.printf(Locale.US,
                "Формула: N = t^2 (1 - p) / (d^2 p), t = %.3f, d = %.1f; условие точности |p_пред - p| < d * p_пред%n",
                AutoMode.T_ALPHA, AutoMode.DELTA);
        System.out.printf(Locale.US, "%-6s %10s %10s %12s %12s %s%n",
                "Итер.", "N", "P отк", "|dP|", "d*P_пред", "Точность");
        for (AutoMode.Iteration it : r.iterations) {
            String diff = Double.isNaN(it.diff()) ? "-" : f(it.diff());
            String thr = Double.isNaN(it.threshold()) ? "-" : f(it.threshold());
            String ok = it.index() == 0 ? "(пристрелка)" : (it.accurate() ? "достигнута" : "нет");
            System.out.printf(Locale.US, "%-6d %10d %10.4f %12s %12s %s%n", it.index(), it.n(), it.p(), diff, thr, ok);
        }
        if (r.converged) System.out.println("Заданная точность достигнута при N = " + r.finalN + ".");
        if (!r.note.isEmpty()) System.out.println(r.note);
    }
}
