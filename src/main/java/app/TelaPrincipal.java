package app;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class TelaPrincipal extends JFrame {

    private PadariaApp app;

    // Cores do tema
    private static final Color COR_FUNDO       = new Color(245, 240, 230);
    private static final Color COR_PRIMARIA     = new Color(139, 90, 43);   // marrom
    private static final Color COR_SECUNDARIA   = new Color(210, 160, 80);  // dourado
    private static final Color COR_TEXTO_CLARO  = Color.WHITE;
    private static final Color COR_VERDE        = new Color(60, 160, 60);
    private static final Color COR_VERMELHO     = new Color(200, 50, 50);

    private JLabel lblVendas, lblDespesas, lblLucro;
    private DefaultTableModel modeloEstoque;
    private JTable tabelaEstoque;

    public TelaPrincipal() {
    app = new PadariaApp();
    app.carregarDados();
    configurarJanela();
    construirUI();
    atualizarTudo();
}

    private void configurarJanela() {
        setTitle("🍞 Sistema Padaria");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        setBackground(COR_FUNDO);
        getContentPane().setBackground(COR_FUNDO);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                app.salvarDados();
            }
        });
    }

    private void construirUI() {
        // ── Painel com imagem de fundo ─────────────────────────
        JPanel painelFundo = new JPanel(new BorderLayout(10, 10)) {
            private Image imagemFundo;
            {
                try {
                    imagemFundo = new javax.swing.ImageIcon(
                        "/home/eric-campos7/Downloads/Padaria.jpeg").getImage();
                } catch (Exception ex) { imagemFundo = null; }
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (imagemFundo != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    // Imagem escurecida para não atrapalhar o conteúdo
                    g2.drawImage(imagemFundo, 0, 0, getWidth(), getHeight(), this);
                    g2.setColor(new Color(245, 240, 230, 200)); // véu bege semitransparente
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            }
        };
        painelFundo.setOpaque(true);
        setContentPane(painelFundo);

        setLayout(new BorderLayout(10, 10));

        // ── Cabeçalho ──────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COR_PRIMARIA);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titulo = new JLabel("🍞 Padaria do Jaime");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(COR_TEXTO_CLARO);
        header.add(titulo, BorderLayout.WEST);

        JLabel subtitulo = new JLabel("Sistema de Gestão");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(COR_SECUNDARIA);
        header.add(subtitulo, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Centro: tabela + painel direito ───────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                criarPainelEstoque(), criarPainelDireito());
        split.setDividerLocation(500);
        split.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        // ── Rodapé ─────────────────────────────────────────────
        add(criarRodape(), BorderLayout.SOUTH);
    }

    // ── Painel Estoque (esquerda) ──────────────────────────────
    private JPanel criarPainelEstoque() {
        JPanel painel = new JPanel(new BorderLayout(5, 5));
        painel.setBackground(COR_FUNDO);

        JLabel titulo = new JLabel("📦 Estoque");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(COR_PRIMARIA);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        painel.add(titulo, BorderLayout.NORTH);

        String[] colunas = {"Produto", "Custo (R$)", "Venda (R$)", "Qtd"};
        modeloEstoque = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaEstoque = new JTable(modeloEstoque);
        estilizarTabela(tabelaEstoque);

        painel.add(new JScrollPane(tabelaEstoque), BorderLayout.CENTER);

        // Botões estoque
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        botoes.setBackground(COR_FUNDO);
        botoes.add(criarBotao("+ Produto", COR_VERDE, e -> dialogAdicionarProduto()));
        botoes.add(criarBotao("✏ Editar",  COR_PRIMARIA, e -> dialogEditarProduto()));
        botoes.add(criarBotao("🗑 Remover", COR_VERMELHO, e -> removerProdutoSelecionado()));
        painel.add(botoes, BorderLayout.SOUTH);

        return painel;
    }

    // ── Painel Direito ─────────────────────────────────────────
    private JPanel criarPainelDireito() {
        JPanel painel = new JPanel(new BorderLayout(5, 10));
        painel.setBackground(COR_FUNDO);

        // Resumo do dia
        JPanel resumo = new JPanel(new GridLayout(3, 1, 5, 5));
        resumo.setBackground(COR_FUNDO);
        resumo.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COR_PRIMARIA), "📊 Resumo do Dia",
                0, 0, new Font("Segoe UI", Font.BOLD, 13), COR_PRIMARIA));

        lblVendas   = criarLabelResumo("Vendas:    R$ 0,00", COR_VERDE);
        lblDespesas = criarLabelResumo("Despesas:  R$ 0,00", COR_VERMELHO);
        lblLucro    = criarLabelResumo("Lucro:     R$ 0,00", COR_PRIMARIA);

        resumo.add(lblVendas);
        resumo.add(lblDespesas);
        resumo.add(lblLucro);

        painel.add(resumo, BorderLayout.NORTH);

        // Ações rápidas
        JPanel acoes = new JPanel(new GridLayout(5, 1, 5, 8));
        acoes.setBackground(COR_FUNDO);
        acoes.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COR_PRIMARIA), "⚡ Ações Rápidas",
                0, 0, new Font("Segoe UI", Font.BOLD, 13), COR_PRIMARIA));

        acoes.add(criarBotaoGrande("🛒  Registrar Venda",    COR_VERDE,     e -> dialogVenda()));
        acoes.add(criarBotaoGrande("💸  Registrar Despesa",  COR_VERMELHO,  e -> dialogDespesa()));
        acoes.add(criarBotaoGrande("📋  Ver Relatório",      COR_PRIMARIA,  e -> mostrarRelatorio()));
        acoes.add(criarBotaoGrande("🔄  Fechar o Dia",       COR_SECUNDARIA, e -> fecharDia()));
        acoes.add(criarBotaoGrande("🔃  Atualizar Tela",     Color.GRAY,    e -> atualizarTudo()));

        painel.add(acoes, BorderLayout.CENTER);
        return painel;
    }

    // ── Rodapé ─────────────────────────────────────────────────
    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rodape.setBackground(COR_PRIMARIA);
        JLabel txt = new JLabel("Sistema Padaria v1.0  •  Desenvolvido com ❤");
        txt.setForeground(COR_SECUNDARIA);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rodape.add(txt);
        return rodape;
    }

    // ── Dialogs ────────────────────────────────────────────────
    private void dialogVenda() {
        List<Produto> estoque = app.getEstoque();
        if (estoque.isEmpty()) { mostrarErro("Nenhum produto no estoque!"); return; }

        String[] nomes = estoque.stream().map(Produto::getNome).toArray(String[]::new);

        JComboBox<String> combo = new JComboBox<>(nomes);
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        JLabel lblPreco = new JLabel("Preço unitário: R$ -");

        combo.addActionListener(e -> {
            Produto p = app.buscarProduto((String) combo.getSelectedItem());
            if (p != null) lblPreco.setText(String.format("Preço unitário: R$ %.2f", p.getPrecoVenda()));
        });
        combo.setSelectedIndex(0);

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Produto:")); p.add(combo);
        p.add(new JLabel("Quantidade:")); p.add(spinner);
        p.add(lblPreco); p.add(new JLabel(""));

        int opt = JOptionPane.showConfirmDialog(this, p, "🛒 Registrar Venda",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION) return;

        String produto = (String) combo.getSelectedItem();
        int qtd = (int) spinner.getValue();
        String resultado = app.registrarVenda(produto, qtd);

        if (resultado.startsWith("OK:")) {
            double valor = Double.parseDouble(resultado.substring(3).replace(",", "."));
            JOptionPane.showMessageDialog(this,
                    String.format("✅ Venda registrada!\n%dx %s\nTotal: R$ %.2f", qtd, produto, valor),
                    "Venda Realizada", JOptionPane.INFORMATION_MESSAGE);
        } else {
            mostrarErro(resultado);
        }
        atualizarTudo();
    }

    private void dialogDespesa() {
        String input = JOptionPane.showInputDialog(this,
                "Valor da despesa (R$):", "💸 Registrar Despesa", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.isBlank()) return;
        try {
            double valor = Double.parseDouble(input.replace(",", "."));
            app.registrarDespesa(valor);
            JOptionPane.showMessageDialog(this,
                    String.format("Despesa de R$ %.2f registrada.", valor),
                    "Despesa Registrada", JOptionPane.INFORMATION_MESSAGE);
            atualizarTudo();
        } catch (NumberFormatException e) {
            mostrarErro("Valor inválido! Use números (ex: 15.50)");
        }
    }

    private void dialogAdicionarProduto() {
        JTextField fNome  = new JTextField();
        JTextField fCusto = new JTextField();
        JTextField fVenda = new JTextField();
        JTextField fQtd   = new JTextField();

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Nome do produto:")); p.add(fNome);
        p.add(new JLabel("Preço de custo (R$):")); p.add(fCusto);
        p.add(new JLabel("Preço de venda (R$):")); p.add(fVenda);
        p.add(new JLabel("Quantidade inicial:")); p.add(fQtd);

        int opt = JOptionPane.showConfirmDialog(this, p, "➕ Adicionar Produto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION) return;

        try {
            String nome   = fNome.getText().trim();
            double custo  = Double.parseDouble(fCusto.getText().replace(",", "."));
            double venda  = Double.parseDouble(fVenda.getText().replace(",", "."));
            int    qtd    = Integer.parseInt(fQtd.getText().trim());

            if (nome.isEmpty()) { mostrarErro("Nome não pode ser vazio!"); return; }
            app.adicionarProduto(new Produto(nome, custo, venda, qtd));
            atualizarTudo();
        } catch (NumberFormatException e) {
            mostrarErro("Preencha os campos corretamente!");
        }
    }

    private void dialogEditarProduto() {
        int row = tabelaEstoque.getSelectedRow();
        if (row < 0) { mostrarErro("Selecione um produto na tabela!"); return; }

        String nome = (String) modeloEstoque.getValueAt(row, 0);
        Produto p = app.buscarProduto(nome);
        if (p == null) return;

        JTextField fCusto = new JTextField(String.format("%.2f", p.getPrecoCusto()));
        JTextField fVenda = new JTextField(String.format("%.2f", p.getPrecoVenda()));
        JTextField fQtd   = new JTextField(String.valueOf(p.getQuantidade()));

        JPanel painel = new JPanel(new GridLayout(3, 2, 8, 8));
        painel.add(new JLabel("Preço de custo (R$):")); painel.add(fCusto);
        painel.add(new JLabel("Preço de venda (R$):")); painel.add(fVenda);
        painel.add(new JLabel("Quantidade:")); painel.add(fQtd);

        int opt = JOptionPane.showConfirmDialog(this, painel, "✏ Editar: " + nome,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION) return;

        try {
            p.setPrecoCusto(Double.parseDouble(fCusto.getText().replace(",", ".")));
            p.setPrecoVenda(Double.parseDouble(fVenda.getText().replace(",", ".")));
            p.setQuantidade(Integer.parseInt(fQtd.getText().trim()));
            app.salvarDados();
            atualizarTudo();
        } catch (NumberFormatException e) {
            mostrarErro("Valores inválidos!");
        }
    }

    private void removerProdutoSelecionado() {
        int row = tabelaEstoque.getSelectedRow();
        if (row < 0) { mostrarErro("Selecione um produto para remover!"); return; }

        String nome = (String) modeloEstoque.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remover o produto \"" + nome + "\"?", "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            app.removerProduto(nome);
            atualizarTudo();
        }
    }

    private void mostrarRelatorio() {
    // Opções de período
    String[] opcoes = {
        "Hoje",
        "Ontem",
        "Últimos 7 dias",
        "Últimos 30 dias",
        "Este mês",
        "Mês anterior",
        "Período personalizado"
    };

    String escolha = (String) JOptionPane.showInputDialog(this,
        "Selecione o período do relatório:",
        "📋 Relatório", JOptionPane.QUESTION_MESSAGE,
        null, opcoes, opcoes[0]);

    if (escolha == null) return;

    java.time.LocalDate hoje = java.time.LocalDate.now();
    java.time.LocalDate inicio, fim;

    switch (escolha) {
        case "Hoje":
            inicio = fim = hoje; break;
        case "Ontem":
            inicio = fim = hoje.minusDays(1); break;
        case "Últimos 7 dias":
            inicio = hoje.minusDays(6); fim = hoje; break;
        case "Últimos 30 dias":
            inicio = hoje.minusDays(29); fim = hoje; break;
        case "Este mês":
            inicio = hoje.withDayOfMonth(1); fim = hoje; break;
        case "Mês anterior":
            java.time.LocalDate mesAnterior = hoje.minusMonths(1);
            inicio = mesAnterior.withDayOfMonth(1);
            fim = mesAnterior.withDayOfMonth(mesAnterior.lengthOfMonth()); break;
        default:
            // Período personalizado
            JTextField dtInicio = new JTextField(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            JTextField dtFim    = new JTextField(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
            p.add(new JLabel("Data início (dd/MM/yyyy):")); p.add(dtInicio);
            p.add(new JLabel("Data fim (dd/MM/yyyy):")); p.add(dtFim);
            if (JOptionPane.showConfirmDialog(this, p, "Período", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            try {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                inicio = java.time.LocalDate.parse(dtInicio.getText().trim(), dtf);
                fim    = java.time.LocalDate.parse(dtFim.getText().trim(), dtf);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Data inválida! Use o formato dd/MM/yyyy", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
    }

    String relatorio = app.gerarRelatorioFiltrado(inicio, fim);
    JTextArea area = new JTextArea(relatorio);
    area.setFont(new Font("Monospaced", Font.PLAIN, 13));
    area.setEditable(false);
    area.setBackground(new Color(250, 245, 235));
    JScrollPane scroll = new JScrollPane(area);
    scroll.setPreferredSize(new Dimension(480, 380));

    JButton btnPDF = new JButton("💾 Salvar PDF");
    btnPDF.addActionListener(e -> salvarRelatorioPDF(relatorio));

    JPanel painel = new JPanel(new BorderLayout(10, 10));
    painel.add(scroll, BorderLayout.CENTER);
    painel.add(btnPDF, BorderLayout.SOUTH);

    JOptionPane.showMessageDialog(this, painel, "📋 Relatório — " + escolha, JOptionPane.PLAIN_MESSAGE);
}

private void salvarRelatorioPDF(String relatorio) {
    JFileChooser fc = new JFileChooser();
    fc.setSelectedFile(new java.io.File("relatorio_padaria.pdf"));
    if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

    try {
        com.itextpdf.text.Document doc = new com.itextpdf.text.Document();
        com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(fc.getSelectedFile()));
        doc.open();
        doc.add(new com.itextpdf.text.Paragraph("RELATÓRIO DA PADARIA",
            com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16)));
        doc.add(new com.itextpdf.text.Paragraph(" "));
        doc.add(new com.itextpdf.text.Paragraph(relatorio,
            com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.COURIER, 11)));
        doc.close();
        JOptionPane.showMessageDialog(this, "✅ PDF salvo com sucesso!\n" + fc.getSelectedFile().getAbsolutePath());
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Erro ao salvar PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}

    private void fecharDia() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Fechar o dia? Os totais serão zerados e o relatório salvo.",
                "Fechar o Dia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            app.zerarDia();
            atualizarTudo();
            JOptionPane.showMessageDialog(this,
                    "✅ Dia fechado! Relatório salvo.", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Atualização ────────────────────────────────────────────
    private void atualizarTudo() {
        // Atualiza tabela
        modeloEstoque.setRowCount(0);
        for (Produto p : app.getEstoque()) {
            modeloEstoque.addRow(new Object[]{
                p.getNome(),
                String.format("%.2f", p.getPrecoCusto()),
                String.format("%.2f", p.getPrecoVenda()),
                p.getQuantidade()
            });
        }

        // Atualiza labels
        double vendas    = app.getTotalVendasDia();
        double despesas  = app.getTotalDespesasDia();
        double lucro     = vendas - despesas;

        lblVendas.setText(String.format("  💰 Vendas:    R$ %.2f", vendas));
        lblDespesas.setText(String.format("  💸 Despesas:  R$ %.2f", despesas));
        lblLucro.setText(String.format("  📈 Lucro:     R$ %.2f", lucro));
        lblLucro.setForeground(lucro >= 0 ? COR_VERDE : COR_VERMELHO);
    }

    // ── Utilitários UI ─────────────────────────────────────────
    private void estilizarTabela(JTable tabela) {
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabela.setRowHeight(26);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabela.getTableHeader().setBackground(COR_PRIMARIA);
        tabela.getTableHeader().setForeground(Color.WHITE);
        tabela.setSelectionBackground(COR_SECUNDARIA);
        tabela.setGridColor(new Color(220, 210, 195));
        tabela.setShowGrid(true);

        // Linha zebrada com texto sempre visível
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (sel) {
                    setBackground(COR_SECUNDARIA);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 238, 225));
                    setForeground(Color.BLACK);
                }
                setOpaque(true);
                return this;
            }
        });
    }

    private JButton criarBotao(String texto, Color cor, ActionListener al) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(cor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    private JButton criarBotaoGrande(String texto, Color cor, ActionListener al) {
        JButton btn = criarBotao(texto, cor, al);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(200, 42));
        return btn;
    }

    private JLabel criarLabelResumo(String texto, Color cor) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(cor);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        return lbl;
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // ── Main ───────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }
}
