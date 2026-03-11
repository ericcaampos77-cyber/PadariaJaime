package app;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PadariaApp {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/padaria";
    private static final String DB_USER = "postgres";
    private static final String DB_SENHA = "padaria123";

    private Connection getConexao() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_SENHA);
    }

    public void carregarDados() {
        try (Connection conn = getConexao(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS produtos (id SERIAL PRIMARY KEY, nome VARCHAR(100) NOT NULL UNIQUE, preco_custo NUMERIC(10,2) NOT NULL, preco_venda NUMERIC(10,2) NOT NULL, quantidade INTEGER NOT NULL DEFAULT 0)");
            stmt.execute("CREATE TABLE IF NOT EXISTS vendas (id SERIAL PRIMARY KEY, produto_id INTEGER, produto_nome VARCHAR(100), quantidade INTEGER NOT NULL, valor_total NUMERIC(10,2) NOT NULL, realizada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS despesas (id SERIAL PRIMARY KEY, descricao VARCHAR(200), valor NUMERIC(10,2) NOT NULL, registrada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS relatorios_dia (id SERIAL PRIMARY KEY, data_hora TIMESTAMP NOT NULL, total_vendas NUMERIC(10,2), total_despesas NUMERIC(10,2), lucro NUMERIC(10,2), relatorio_texto TEXT)");
            System.out.println("Banco de dados pronto!");
        } catch (SQLException e) {
            System.out.println("Erro ao inicializar banco: " + e.getMessage());
        }
    }

    public void salvarDados() {
        System.out.println("Dados salvos no PostgreSQL!");
    }

    public void adicionarProduto(Produto p) {
        try (Connection conn = getConexao()) {
            PreparedStatement check = conn.prepareStatement("SELECT id FROM produtos WHERE LOWER(nome) = LOWER(?)");
            check.setString(1, p.getNome());
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE produtos SET preco_custo=?, preco_venda=?, quantidade=quantidade+? WHERE LOWER(nome)=LOWER(?)");
                ps.setDouble(1, p.getPrecoCusto());
                ps.setDouble(2, p.getPrecoVenda());
                ps.setInt(3, p.getQuantidade());
                ps.setString(4, p.getNome());
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO produtos (nome, preco_custo, preco_venda, quantidade) VALUES (?,?,?,?)");
                ps.setString(1, p.getNome());
                ps.setDouble(2, p.getPrecoCusto());
                ps.setDouble(3, p.getPrecoVenda());
                ps.setInt(4, p.getQuantidade());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public List<Produto> getEstoque() {
        List<Produto> lista = new ArrayList<>();
        try (Connection conn = getConexao(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM produtos ORDER BY nome"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Produto(rs.getString("nome"), rs.getDouble("preco_custo"), rs.getDouble("preco_venda"), rs.getInt("quantidade")));
            }
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
        return lista;
    }

    public Produto buscarProduto(String nome) {
        try (Connection conn = getConexao(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM produtos WHERE LOWER(nome) = LOWER(?)")) {
            ps.setString(1, nome);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Produto(rs.getString("nome"), rs.getDouble("preco_custo"), rs.getDouble("preco_venda"), rs.getInt("quantidade"));
            }
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
        return null;
    }

    public String registrarVenda(String nomeProduto, int quantidade) {
        Produto p = buscarProduto(nomeProduto);
        if (p == null) {
            return "Produto não encontrado.";
        }
        if (p.getQuantidade() < quantidade) {
            return "Estoque insuficiente! Temos: " + p.getQuantidade();
        }
        double total = p.getPrecoVenda() * quantidade;
        try (Connection conn = getConexao()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps1 = conn.prepareStatement("UPDATE produtos SET quantidade = quantidade - ? WHERE LOWER(nome) = LOWER(?)");
                ps1.setInt(1, quantidade);
                ps1.setString(2, nomeProduto);
                ps1.executeUpdate();
                PreparedStatement ps2 = conn.prepareStatement("INSERT INTO vendas (produto_nome, quantidade, valor_total) VALUES (?,?,?)");
                ps2.setString(1, nomeProduto);
                ps2.setInt(2, quantidade);
                ps2.setDouble(3, total);
                ps2.executeUpdate();
                conn.commit();
                return String.format("Venda registrada! %dx %s = R$ %.2f", quantidade, nomeProduto, total);
            } catch (SQLException e) {
                conn.rollback();
                return "Erro: " + e.getMessage();
            }
        } catch (SQLException e) {
            return "Erro: " + e.getMessage();
        }
    }

    public void registrarDespesa(double valor) {
        registrarDespesa("Despesa geral", valor);
    }

    public void registrarDespesa(String descricao, double valor) {
        try (Connection conn = getConexao(); PreparedStatement ps = conn.prepareStatement("INSERT INTO despesas (descricao, valor) VALUES (?,?)")) {
            ps.setString(1, descricao);
            ps.setDouble(2, valor);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void removerProduto(String nome) {
        try (Connection conn = getConexao(); PreparedStatement ps = conn.prepareStatement("DELETE FROM produtos WHERE LOWER(nome) = LOWER(?)")) {
            ps.setString(1, nome);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public double getTotalVendasDia() {
        try (Connection conn = getConexao(); PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(valor_total),0) FROM vendas WHERE DATE(realizada_em) = CURRENT_DATE"); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
        return 0;
    }

    public double getTotalDespesasDia() {
        try (Connection conn = getConexao(); PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(valor),0) FROM despesas WHERE DATE(registrada_em) = CURRENT_DATE"); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
        return 0;
    }

    public String gerarRelatorioDia() {
        double vendas = getTotalVendasDia(), despesas = getTotalDespesasDia(), lucro = vendas - despesas;
        StringBuilder sb = new StringBuilder();
        sb.append("=== RELATÓRIO DO DIA ===\n");
        sb.append("Data/Hora: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        sb.append(String.format("Vendas totais:   R$ %.2f%n", vendas));
        sb.append(String.format("Despesas totais: R$ %.2f%n", despesas));
        sb.append(String.format("Lucro do dia:    R$ %.2f%n", lucro));
        sb.append("\nEstoque atual:\n");
        for (Produto p : getEstoque()) {
            sb.append(String.format("  - %-20s | Custo: R$ %.2f | Venda: R$ %.2f | Qtd: %d%n", p.getNome(), p.getPrecoCusto(), p.getPrecoVenda(), p.getQuantidade()));
        }
        return sb.toString();
    }

    public void zerarDia() {
        double vendas = getTotalVendasDia(), despesas = getTotalDespesasDia(), lucro = vendas - despesas;
        String relatorio = gerarRelatorioDia();
        try (Connection conn = getConexao(); PreparedStatement ps = conn.prepareStatement("INSERT INTO relatorios_dia (data_hora, total_vendas, total_despesas, lucro, relatorio_texto) VALUES (?,?,?,?,?)")) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setDouble(2, vendas);
            ps.setDouble(3, despesas);
            ps.setDouble(4, lucro);
            ps.setString(5, relatorio);
            ps.executeUpdate();
            System.out.println("Dia zerado!");
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
public String gerarRelatorioFiltrado(java.time.LocalDate inicio, java.time.LocalDate fim) {
    double vendas = 0, despesas = 0;
    StringBuilder detalhesVendas = new StringBuilder();
    StringBuilder detalhesDespesas = new StringBuilder();

    try (Connection conn = getConexao()) {
        // Vendas no período
        PreparedStatement ps1 = conn.prepareStatement(
            "SELECT produto_nome, SUM(quantidade) as qtd, SUM(valor_total) as total FROM vendas WHERE DATE(realizada_em) BETWEEN ? AND ? GROUP BY produto_nome ORDER BY total DESC");
        ps1.setDate(1, java.sql.Date.valueOf(inicio));
        ps1.setDate(2, java.sql.Date.valueOf(fim));
        ResultSet rs1 = ps1.executeQuery();
        while (rs1.next()) {
            vendas += rs1.getDouble("total");
            detalhesVendas.append(String.format("  %-20s %dx = R$ %.2f%n",
                rs1.getString("produto_nome"), rs1.getInt("qtd"), rs1.getDouble("total")));
        }

        // Despesas no período
        PreparedStatement ps2 = conn.prepareStatement(
            "SELECT descricao, SUM(valor) as total FROM despesas WHERE DATE(registrada_em) BETWEEN ? AND ? GROUP BY descricao ORDER BY total DESC");
        ps2.setDate(1, java.sql.Date.valueOf(inicio));
        ps2.setDate(2, java.sql.Date.valueOf(fim));
        ResultSet rs2 = ps2.executeQuery();
        while (rs2.next()) {
            despesas += rs2.getDouble("total");
            detalhesDespesas.append(String.format("  %-20s R$ %.2f%n",
                rs2.getString("descricao"), rs2.getDouble("total")));
        }
    } catch (SQLException e) {
        return "Erro ao gerar relatório: " + e.getMessage();
    }

    double lucro = vendas - despesas;
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    StringBuilder sb = new StringBuilder();
    sb.append("========================================\n");
    sb.append("         RELATÓRIO PADARIA JAIME\n");
    sb.append("========================================\n");
    sb.append(String.format("Período: %s até %s%n", inicio.format(fmt), fim.format(fmt)));
    sb.append("----------------------------------------\n");
    sb.append("VENDAS:\n");
    sb.append(detalhesVendas.length() > 0 ? detalhesVendas : "  Nenhuma venda no período.\n");
    sb.append(String.format("  TOTAL VENDAS:    R$ %.2f%n", vendas));
    sb.append("----------------------------------------\n");
    sb.append("DESPESAS:\n");
    sb.append(detalhesDespesas.length() > 0 ? detalhesDespesas : "  Nenhuma despesa no período.\n");
    sb.append(String.format("  TOTAL DESPESAS:  R$ %.2f%n", despesas));
    sb.append("========================================\n");
    sb.append(String.format("  LUCRO DO PERÍODO: R$ %.2f%n", lucro));
    sb.append("========================================\n");
    return sb.toString();
}
}
