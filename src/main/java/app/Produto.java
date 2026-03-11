package app;

public class Produto {
    private String nome;
    private double precoCusto;
    private double precoVenda;
    private int quantidade;

    public Produto(String nome, double precoCusto, double precoVenda, int quantidade) {
        this.nome = nome;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.quantidade = quantidade;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(double precoCusto) { this.precoCusto = precoCusto; }

    public double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(double precoVenda) { this.precoVenda = precoVenda; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    @Override
    public String toString() {
        return String.format("%s | Custo: R$ %.2f | Venda: R$ %.2f | Qtd: %d",
                nome, precoCusto, precoVenda, quantidade);
    }
}