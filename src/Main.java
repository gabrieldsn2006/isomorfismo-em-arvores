/**
 * Ponto de entrada do programa de verificação de isomorfismo em árvores.
 *
 * Uso:
 *   java Main <arquivo1.txt> <arquivo2.txt>
 *
 * Cada arquivo deve seguir o formato algs4 para grafos não direcionados:
 *   V        <- número de vértices
 *   E        <- número de arestas
 *   v1 w1    <- aresta entre v1 e w1
 *   ...
 */
public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                "informe dois arquivos de entrada. Ex.: java Main ../dados/arvore1.txt ../dados/arvore2.txt"
            );
        }

        // ── Leitura dos grafos ──────────────────────────────────────────────
        Graph tree1 = new Graph(new In(args[0]));
        Graph tree2 = new Graph(new In(args[1]));

        // ── Lista de adjacência ─────────────────────────────────────────────
        StdOut.println("ARVORE 1: " + args[0]);
        StdOut.println(tree1);

        StdOut.println("ARVORE 2: " + args[1]);
        StdOut.println(tree2);

        // ── Criação dos objetos de análise ──────────────────────────────────
        TreeIsomorphism analysis1 = new TreeIsomorphism(tree1);
        TreeIsomorphism analysis2 = new TreeIsomorphism(tree2);

        // ── Validação ───────────────────────────────────────────────────────
        StdOut.println("VALIDACAO DAS ENTRADAS:");
        StdOut.println("> Arvore 1: " + analysis1.getValidationMessage());
        StdOut.println("> Arvore 2: " + analysis2.getValidationMessage());
        StdOut.println();

        if (!analysis1.isTree()) {
            StdOut.println("\u001B[31m[ERRO] A entrada 1 nao representa uma arvore valida.\u001B[0m");
            StdOut.println("Comparacao interrompida.");
            return;
        }

        if (!analysis2.isTree()) {
            StdOut.println("\u001B[31m[ERRO] A entrada 2 nao representa uma arvore valida.\u001B[0m");
            StdOut.println("Comparacao interrompida.");
            return;
        }

        // ── Centros ─────────────────────────────────────────────────────────
        StdOut.println("CENTROS:");

        int[] centers1 = analysis1.getCenters();
        int[] centers2 = analysis2.getCenters();

        StdOut.print("> Arvore 1 - centro(s): ");
        printArray(centers1);

        StdOut.print("> Arvore 2 - centro(s): ");
        printArray(centers2);

        StdOut.println();

        // ── Codificação canônica ────────────────────────────────────────────
        StdOut.println("CODIFICACAO CANONICA:");

        String code1 = analysis1.getCanonicalEncoding();
        String code2 = analysis2.getCanonicalEncoding();

        StdOut.println("> Arvore 1: " + code1);
        StdOut.println("> Arvore 2: " + code2);
        StdOut.println();

        // ── Veredito final ──────────────────────────────────────────────────
        StdOut.println("VEREDITO FINAL:");

        if (code1.equals(code2)) {
            StdOut.println("> As duas arvores \u001B[34mSAO ISOMORFAS\u001B[0m.");
            StdOut.println("> Justificativa: ambas produziram a mesma codificacao canonica.");
            StdOut.println("> Codigo: " + code1);
        } else {
            StdOut.println("> As duas arvores \u001B[31mNAO SAO ISOMORFAS\u001B[0m.");
            StdOut.println("> Justificativa: as codificacoes canonicas sao diferentes.");
            StdOut.println("> Codigo 1: " + code1);
            StdOut.println("> Codigo 2: " + code2);
        }
    }

    /** Imprime um array de inteiros no formato { a, b, ... } */
    private static void printArray(int[] arr) {
        StringBuilder sb = new StringBuilder("{ ");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) sb.append(", ");
        }
        sb.append(" }");
        StdOut.println(sb.toString());
    }
}