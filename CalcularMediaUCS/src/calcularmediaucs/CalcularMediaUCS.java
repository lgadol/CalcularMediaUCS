package calcularmediaucs;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.text.NumberFormat;
import java.text.ParseException;

import java.util.Locale;

public class CalcularMediaUCS {
    private static final String INSERT_NOTAS_SQL =
        "INSERT INTO Notas" +
        "  (notaUA, questionario, notaProva, notaPrimeiraEtapa, avaliacaoPrimeiraEtapa, notaSegundaEtapa, avaliacaoSegundaEtapa, mediaFinal_disciplina, mediaFinal_projeto) VALUES " +
        " (?, ?, ?, ?, ?, ?, ?, ?, ?);";

    public static void main(String[] args) throws SQLException {
        JFrame frame = new JFrame("Calculadora de Média");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);

        frame.setVisible(true);
    }

    private static double getNota(String message, double max) {
        double nota = -1;
        while (nota < 0 || nota > max) {
            String notaString = JOptionPane.showInputDialog(message).replace(',', '.');
            try {
                nota = NumberFormat.getInstance(Locale.US).parse(notaString).doubleValue();
                if (nota < 0 || nota > max) {
                    JOptionPane.showMessageDialog(null,
                                                  "Erro: A nota não pode ser menor que 0 ou maior que " + max +
                                                  ". Tente novamente.");
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(null, "Erro: Entrada inválida. Tente novamente.");
            }
        }
        return nota;
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JButton disciplinasButton = new JButton("Média Disciplinas");
        disciplinasButton.setBounds(10, 10, 250, 25);
        panel.add(disciplinasButton);
        disciplinasButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double notaUA = getNota("Insira a nota nas Unidades de Aprendizagem (UA):", 10);
                double questionario = getNota("Insira a nota no Questionário Avaliativo:", 10);
                double notaProva = getNota("Insira a melhor nota na Prova ou Exame:", 10);

                double mediaFinal_disciplina = (notaUA * 0.2) + (questionario * 0.2) + (notaProva * 0.6);
                JOptionPane.showMessageDialog(null, "A média final é: " + mediaFinal_disciplina);

                try {
                    insertNotas(notaUA, questionario, notaProva, 0, 0, 0, 0, mediaFinal_disciplina, 0);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            }
        });

        JButton projetoButton = new JButton("Média Projeto Integrador");
        projetoButton.setBounds(10, 40, 250, 25);
        panel.add(projetoButton);
        projetoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double notaPrimeiraEtapa = getNota("Insira a nota na Primeira Etapa:", 8);
                double avaliacaoPrimeiraEtapa = getNota("Insira a nota na Avaliação Por Pares - Primeira Etapa:", 2);
                double notaSegundaEtapa = getNota("Insira a nota na Segunda Etapa:", 8);
                double avaliacaoSegundaEtapa = getNota("Insira a nota na Avaliação Por Pares - Segunda Etapa:", 2);

                double mediaFinal_projeto =
                    ((notaPrimeiraEtapa + avaliacaoPrimeiraEtapa) * 0.4) +
                    ((notaSegundaEtapa + avaliacaoSegundaEtapa) * 0.6);
                JOptionPane.showMessageDialog(null, "A média final é: " + mediaFinal_projeto);

                try {
                    insertNotas(0, 0, 0, notaPrimeiraEtapa, avaliacaoPrimeiraEtapa, notaSegundaEtapa,
                                avaliacaoSegundaEtapa, 0, mediaFinal_projeto);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            }
        });
    }

    public static void insertNotas(double notaUA, double questionario, double notaProva, double notaPrimeiraEtapa,
                                   double avaliacaoPrimeiraEtapa, double notaSegundaEtapa, double avaliacaoSegundaEtapa,
                                   double mediaFinal_disciplina, double mediaFinal_projeto) throws SQLException {
        System.out.println(INSERT_NOTAS_SQL);

        // Conexão com DB
        try (Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "pedro", "PG08E11A");


        // Cria um statement usando a conexão
        PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NOTAS_SQL)) {
            preparedStatement.setDouble(1, notaUA);
            preparedStatement.setDouble(2, questionario);
            preparedStatement.setDouble(3, notaProva);
            preparedStatement.setDouble(4, notaPrimeiraEtapa);
            preparedStatement.setDouble(5, avaliacaoPrimeiraEtapa);
            preparedStatement.setDouble(6, notaSegundaEtapa);
            preparedStatement.setDouble(7, avaliacaoSegundaEtapa);
            preparedStatement.setDouble(8, mediaFinal_disciplina);
            preparedStatement.setDouble(9, mediaFinal_projeto);

            System.out.println(preparedStatement);

            // Executa o statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public static void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
}