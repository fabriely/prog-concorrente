import java.util.Random;

public class Jogo {
    private static final int MAX_TENTATIVAS = 50;
    private static final int TAMPINHAS_INICIAIS = 5;

    private static final Object mutex = new Object();
    private static final Random random = new Random();

    private static int tampinhasA = TAMPINHAS_INICIAIS;
    private static int tampinhasB = TAMPINHAS_INICIAIS;
    private static int tentativas = 0;
    private static boolean jogoEncerrado = false;

    private static class Jogador implements Runnable {
        private final String nome;

        Jogador(String nome) {
            this.nome = nome;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (mutex) {
                    if (jogoEncerrado || tentativas >= MAX_TENTATIVAS || tampinhasA == 0 || tampinhasB == 0) {
                        jogoEncerrado = true;
                        break;
                    }

                    boolean acertou = random.nextBoolean();
                    if (nome.equals("A")) {
                        if (acertou) {
                            if (tampinhasA > 0) {
                                tampinhasA--;
                                tampinhasB++;
                            }
                            System.out.println("A acertou! A:" + tampinhasA + " | B:" + tampinhasB);
                        } else {
                            System.out.println("A errou! A:" + tampinhasA + " | B:" + tampinhasB);
                        }
                    } else {
                        if (acertou) {
                            if (tampinhasB > 0) {
                                tampinhasB--;
                                tampinhasA++;
                            }
                            System.out.println("B acertou! A:" + tampinhasA + " | B:" + tampinhasB);
                        } else {
                            System.out.println("B errou! A:" + tampinhasA + " | B:" + tampinhasB);
                        }
                    }

                    tentativas++;
                    if (tentativas >= MAX_TENTATIVAS || tampinhasA == 0 || tampinhasB == 0) {
                        jogoEncerrado = true;
                        break;
                    }
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread jogadorA = new Thread(new Jogador("A"));
        Thread jogadorB = new Thread(new Jogador("B"));

        jogadorA.start();
        jogadorB.start();

        jogadorA.join();
        jogadorB.join();

        System.out.println("\n=== RESULTADO FINAL ===");
        System.out.println("Jogador A: " + tampinhasA + " tampinhas");
        System.out.println("Jogador B: " + tampinhasB + " tampinhas");

        String vencedor;
        if (tampinhasA > tampinhasB) {
            vencedor = "Jogador B";
        } else if (tampinhasB > tampinhasA) {
            vencedor = "Jogador A";
        } else {
            vencedor = "Empate";
        }
        System.out.println("Vencedor: " + vencedor);
    }
}
