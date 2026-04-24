#  T6 - Identificacao de Isomorfismo em Arvores

Implementacao em **Java** da base inicial do Trabalho Pratico 6 da disciplina
**Resolucao de Problemas com Grafos**.

## Video

Link do video explicativo: https://www.youtube.com/watch?v=f9UwGXu4_yQ

## Estrutura

```text
T6/
├── README.md
├── T6.md
├── youtube_vides.md
├── dados/
│   ├── invalid-ciclo3.txt
│   ├── iso-path4-a.txt
│   ├── iso-path4-b.txt
│   ├── nao-iso-estrela5.txt
│   ├── nao-iso-path5.txt
│   ├── unico-centro-a.txt
│   └── unico-centro-b.txt
└── src/
    ├── Bag.java
    ├── Graph.java
    ├── In.java
    ├── Main.java
    ├── Stack.java
    ├── StdIn.java
    ├── StdOut.java
    └── TreeIsomorphism.java
```


## Compilacao

Execute o comando a seguir para entrar no diretório /src:

```bash
cd src
```

Execute (no src) o comando a seguir para compilar o código:

```bash
javac -d ../out *.java
```

## Execucao

Fixtures de teste:

```bash
java -cp ../out Main ../dados/iso-path4-a.txt ../dados/iso-path4-b.txt
```
```bash
java -cp ../out Main ../dados/nao-iso-path5.txt ../dados/nao-iso-estrela5.txt
```
```bash
java -cp ../out Main ../dados/unico-centro-a.txt ../dados/unico-centro-b.txt
```
```bash
java -cp ../out Main ../dados/invalid-ciclo3.txt ../dados/iso-path4-a.txt
```

> **Obs. (PowerShell):** Caso os acentos e caracteres especiais não apareçam corretamente no terminal, execute o comando abaixo antes de rodar o programa para forçar o encoding UTF-8:
> ```bash
> [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
> ```
