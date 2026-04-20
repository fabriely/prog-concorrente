import java.util.concurrent.Semaphore;

public class ProdutorConsumidor {
    private static final int BUFFER_SIZE = 10;
    private static final int NUM_MESSAGES = 50;
    private static final int NUM_PRODUCERS = 3;

    private static class Message {
        int threadId;
        String threadName;
        int messageIndex;
        String text;

        Message(int threadId, String threadName, int messageIndex, String text) {
            this.threadId = threadId;
            this.threadName = threadName;
            this.messageIndex = messageIndex;
            this.text = text;
        }
    }

    private static class BufferCircular {
        private final Message[] buffer = new Message[BUFFER_SIZE];
        private int in = 0;
        private int out = 0;

        private final Semaphore empty = new Semaphore(BUFFER_SIZE);
        private final Semaphore full = new Semaphore(0);
        private final Semaphore mutex = new Semaphore(1);

        public void put(Message message) throws InterruptedException {
            empty.acquire();
            mutex.acquire();
            buffer[in] = message;
            in = (in + 1) % BUFFER_SIZE;
            mutex.release();
            full.release();
        }

        public Message take() throws InterruptedException {
            full.acquire();
            mutex.acquire();
            Message message = buffer[out];
            out = (out + 1) % BUFFER_SIZE;
            mutex.release();
            empty.release();
            return message;
        }
    }

    private static class Produtor implements Runnable {
        private final int id;
        private final String nome;
        private final BufferCircular buffer;

        Produtor(int id, String nome, BufferCircular buffer) {
            this.id = id;
            this.nome = nome;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= NUM_MESSAGES; i++) {
                    String texto = "mensagem de numero " + i;
                    Message message = new Message(id, nome, i, texto);
                    buffer.put(message);
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class Consumidor implements Runnable {
        private final BufferCircular buffer;
        private final int totalMensagens;

        Consumidor(BufferCircular buffer, int totalMensagens) {
            this.buffer = buffer;
            this.totalMensagens = totalMensagens;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < totalMensagens; i++) {
                    Message message = buffer.take();
                    System.out.println("[Thread " + message.threadId + "]: " + message.threadName + " " + message.text);
                    Thread.sleep(30);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BufferCircular buffer = new BufferCircular();

        Thread[] produtores = new Thread[NUM_PRODUCERS];
        produtores[0] = new Thread(new Produtor(1, "audio thread", buffer));
        produtores[1] = new Thread(new Produtor(2, "video thread", buffer));
        produtores[2] = new Thread(new Produtor(3, "input thread", buffer));

        Thread consumidor = new Thread(new Consumidor(buffer, NUM_PRODUCERS * NUM_MESSAGES));
        consumidor.setName("log thread");

        consumidor.start();
        for (Thread produtor : produtores) {
            produtor.start();
        }

        for (Thread produtor : produtores) {
            produtor.join();
        }
        consumidor.join();

        System.out.println("Fim da execucao.");
    }
}
