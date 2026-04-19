import java.util.Random;

public class LeitoresEscritores {
    private static final int TOTAL_LEITORES = 3;
    private static final int TOTAL_ESCRITORES = 3;

    private static final Random random = new Random();

    private static class MonitorArtigo {
        private int leitoresAtivos = 0;
        private boolean escrevendo = false;

        public synchronized void entrarLeitor(int id) throws InterruptedException {
            while (escrevendo) {
                wait();
            }
            leitoresAtivos++;
            System.out.println("Leitor " + id + " entrou. Leitores ativos: " + leitoresAtivos);
        }

        public synchronized void sairLeitor(int id) {
            leitoresAtivos--;
            System.out.println("Leitor " + id + " saiu. Leitores ativos: " + leitoresAtivos);
            if (leitoresAtivos == 0) {
                notifyAll();
            }
        }

        public synchronized void entrarEscritor(int id) throws InterruptedException {
            while (escrevendo || leitoresAtivos > 0) {
                wait();
            }
            escrevendo = true;
            System.out.println("Escritor " + id + " entrou.");
        }

        public synchronized void sairEscritor(int id) {
            escrevendo = false;
            System.out.println("Escritor " + id + " saiu.");
            notifyAll();
        }
    }

    private static class Leitor implements Runnable {
        private final int id;
        private final MonitorArtigo monitor;

        Leitor(int id, MonitorArtigo monitor) {
            this.id = id;
            this.monitor = monitor;
        }

        @Override
        public void run() {
            try {
                monitor.entrarLeitor(id);
                Thread.sleep(300 + random.nextInt(700));
                monitor.sairLeitor(id);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class Escritor implements Runnable {
        private final int id;
        private final MonitorArtigo monitor;

        Escritor(int id, MonitorArtigo monitor) {
            this.id = id;
            this.monitor = monitor;
        }

        @Override
        public void run() {
            try {
                monitor.entrarEscritor(id);
                Thread.sleep(400 + random.nextInt(800));
                monitor.sairEscritor(id);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        MonitorArtigo monitor = new MonitorArtigo();
        Thread[] leitores = new Thread[TOTAL_LEITORES];
        Thread[] escritores = new Thread[TOTAL_ESCRITORES];

        for (int i = 0; i < TOTAL_LEITORES; i++) {
            leitores[i] = new Thread(new Leitor(i + 1, monitor));
            leitores[i].start();
        }

        for (int i = 0; i < TOTAL_ESCRITORES; i++) {
            escritores[i] = new Thread(new Escritor(i + 1, monitor));
            escritores[i].start();
        }

        for (int i = 0; i < TOTAL_LEITORES; i++) {
            leitores[i].join();
        }
        for (int i = 0; i < TOTAL_ESCRITORES; i++) {
            escritores[i].join();
        }

        System.out.println("Todos os leitores e escritores finalizaram.");
    }
}
