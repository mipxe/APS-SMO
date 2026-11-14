package smo;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);

        System.out.println("СМО «Сервис распределения заказов Яндекс Такси»");
        System.out.println("Параметры (Enter - значение по умолчанию):");
        int nSources = readInt(in, "Число источников", 3, 1);
        int nDevices = readInt(in, "Число приборов", 3, 1);
        int capacity = readInt(in, "Ёмкость буфера", 5, 1);
        double lambda = readDouble(in, "Интенсивность каждого источника lambda", 0.5);
        double mu = readDouble(in, "Интенсивность обслуживания прибора mu", 0.4);
        int maxRequests = readInt(in, "Число заявок для генерации (всего)", 20, 1);
        long seed = readInt(in, "Seed генератора случайных чисел", 42, Integer.MIN_VALUE);

        Simulation sim = new Simulation(nSources, nDevices, capacity, lambda, mu, maxRequests, seed);

        System.out.print("Режим: [1] пошаговый, [2] автоматический (по умолчанию 1): ");
        String mode = in.hasNextLine() ? in.nextLine().trim() : "1";

        if (mode.equals("2")) {
            SummaryTable table = sim.runAuto();
            ConsolePrinter.printSummary(table);
            return;
        }

        ConsolePrinter.printStep(sim.initialRecord());
        label:
        while (!sim.isFinished()) {
            System.out.print("\n[Enter] - следующий шаг, [a] - дойти до конца, [q] - выход: ");
            String cmd = in.hasNextLine() ? in.nextLine().trim().toLowerCase() : "a";
            switch (cmd) {
                case "n":
                    ConsolePrinter.printStep(sim.step());
                    break;
                case "a":
                    while (!sim.isFinished()) ConsolePrinter.printStep(sim.step());
                    break label;
                case "q":
                    return;
                default:
                    break;
            }
        }
        System.out.println("\nМоделирование завершено, t = " + String.format(java.util.Locale.US, "%.4f", sim.getCurrentTime()));
        ConsolePrinter.printSummary(sim.getSummary());
    }

    private static int readInt(Scanner in, String prompt, int def, int min) {
        while (true) {
            System.out.print(prompt + " [" + def + "]: ");
            if (!in.hasNextLine()) return def;
            String s = in.nextLine().trim();
            if (s.isEmpty()) return def;
            try {
                int v = Integer.parseInt(s);
                if (v >= min) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("  Некорректное значение (целое >= " + min + ")");
        }
    }

    private static double readDouble(Scanner in, String prompt, double def) {
        while (true) {
            System.out.print(prompt + " [" + def + "]: ");
            if (!in.hasNextLine()) return def;
            String s = in.nextLine().trim().replace(',', '.');
            if (s.isEmpty()) return def;
            try {
                double v = Double.parseDouble(s);
                if (v > 0) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("  Некорректное значение (число > 0)");
        }
    }
}
