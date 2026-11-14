package smo;

import java.util.Locale;

public class ConsolePrinter {
    public static boolean SHOW_CALENDAR = true;

    private static String f(double v) {
        return String.format(Locale.US, "%.4f", v);
    }

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
        System.out.println("\n==================== ИТОГОВАЯ СВОДНАЯ ТАБЛИЦА (ОР2) ====================");
        System.out.println("Время моделирования: " + f(t.tEnd()));
        System.out.println();
        System.out.printf(Locale.US, "%-8s %9s %8s %9s %9s %10s %10s %10s %10s %10s%n",
                "Источник", "Создано", "Отказов", "Обслужено", "P отказа",
                "T преб.", "T ожид.", "T обсл.", "D ожид.", "D обсл.");
        for (int i = 0; i <= nSrc; i++) {
            String name = i == nSrc ? "ИТОГО" : "И" + i;
            System.out.printf(Locale.US, "%-8s %9d %8d %9d %9.4f %10.4f %10.4f %10.4f %10.4f %10.4f%n",
                    name, t.generated()[i], t.rejected()[i], t.processed()[i], t.rejectProbability()[i],
                    t.avgSystem()[i], t.avgWait()[i], t.avgService()[i], t.waitVariance()[i], t.serviceVariance()[i]);
        }
        System.out.println();
        System.out.println("Коэффициент использования приборов:");
        for (int i = 0; i < t.deviceLoad().length; i++) {
            System.out.printf(Locale.US, "  П%d: %.4f%n", i, t.deviceLoad()[i]);
        }
        System.out.println("\nT преб. = T ожид. + T обсл. (по обслуженным заявкам); D - дисперсия.");
    }
}
