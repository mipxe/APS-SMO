package smo;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    private static BufferedReader in;

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        System.out.println("СМО «Сервис распределения заказов Яндекс Такси»");
        String mode = prompt("Режим: [1] пошаговый, [2] автоматический (по умолчанию 1): ");
        if (mode == null || mode.isEmpty()) mode = "1";
        boolean auto = mode.equals("2");

        System.out.println("Параметры (Enter - значение по умолчанию):");
        int nSources = readInt("Число источников", 3, 1);
        int nDevices = readInt("Число приборов", 3, 1);
        int capacity = readInt("Ёмкость буфера", 5, 1);
        double lambda = readDouble("Интенсивность каждого источника lambda", 0.5);
        double mu = readDouble("Интенсивность обслуживания прибора mu", 0.4);

        if (auto) {
            long seed = readInt("Seed генератора случайных чисел", 42, Integer.MIN_VALUE);
            AutoMode.Result res = new AutoMode(nSources, nDevices, capacity, lambda, mu, seed).run();
            ConsolePrinter.printAutoIterations(res);
            ConsolePrinter.printSummary(res.table);
            return;
        }

        int maxRequests = readInt("Число заявок для генерации (всего)", 20, 1);
        long seed = readInt("Seed генератора случайных чисел", 42, Integer.MIN_VALUE);
        Simulation sim = new Simulation(nSources, nDevices, capacity, lambda, mu, maxRequests, seed);

        ConsolePrinter.printStep(sim.initialRecord());
        while (!sim.isFinished()) {
            System.out.println("\n[Enter или n] - следующий шаг, [a] - дойти до конца, [q] - выход:");
            System.out.flush();
            String cmd = in.readLine();
            cmd = cmd == null ? null : cmd.trim();
            if (cmd == null) cmd = "a";
            cmd = cmd.toLowerCase();
            if (cmd.equals("q")) return;
            if (cmd.equals("a")) {
                while (!sim.isFinished()) sim.step();
                break;
            }
            ConsolePrinter.printStep(sim.step());
        }
        System.out.println("\nМоделирование завершено, t = " + String.format(java.util.Locale.US, "%.4f", sim.getCurrentTime()));
    }

    private static String prompt(String text) throws IOException {
        System.out.print(text);
        System.out.flush();
        String line = in.readLine();
        return line == null ? null : line.trim();
    }

    private static int readInt(String text, int def, int min) throws IOException {
        while (true) {
            String s = prompt(text + " [" + def + "]: ");
            if (s == null || s.isEmpty()) return def;
            try {
                int v = Integer.parseInt(s);
                if (v >= min) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("  Некорректное значение (целое >= " + min + ")");
        }
    }

    private static double readDouble(String text, double def) throws IOException {
        while (true) {
            String s = prompt(text + " [" + def + "]: ");
            if (s == null || s.isEmpty()) return def;
            try {
                double v = Double.parseDouble(s.replace(',', '.'));
                if (v > 0) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("  Некорректное значение (число > 0)");
        }
    }
}
