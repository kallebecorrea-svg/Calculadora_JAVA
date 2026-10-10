import javax.swing.*; // Importa os componentes gráficos Swing
import java.awt.*; // Importa classes de layout e posicionamento da interface
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Calculadora {

    // Guarda o valor que foi digitado antes do operador
    private static double valorAnterior = 0;

    // Armazena o operador da operação matemática atual (+, -, *, /)
    private static String operador = "";

    // Indica se o próximo clique deve começar um novo número
    private static boolean novoNumero = true;

    public static void main(String[] args) {
        // Lista dos botões visíveis na calculadora
        String[] teclas = {
                "CE", "back", "%", "/",
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "+/-", "0", ".", "="
        };

        // Cria a janela principal da aplicação
        JFrame janela = new JFrame("Calculadora");
        janela.setSize(360, 480); // Define a largura e a altura da janela
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fecha o programa ao encerrar
        janela.setLayout(new BorderLayout(10, 10));
        ((JPanel) janela.getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        // Cria o visor da calculadora, onde os números e resultados aparecem
        JTextField visor = new JTextField("0");
        visor.setEditable(false); // Não permite digitar manualmente no visor
        visor.setHorizontalAlignment(JTextField.RIGHT); // Alinha o texto à direita
        visor.setFont(new Font("SansSerif", Font.BOLD, 32)); // Aumenta o tamanho da fonte
        visor.setBackground(Color.LIGHT_GRAY);
        visor.setPreferredSize(new Dimension(0, 60));
        janela.add(visor, BorderLayout.NORTH); // Coloca o visor na parte superior da janela

        // Painel com layout em grade 4x4 para organizar os botões
        JPanel painel = new JPanel(new GridLayout(5, 4, 10, 10));

        // Cria cada botão da calculadora e define a ação de cada clique
        for (String t : teclas) {
            JButton botao = new JButton(t);

            botao.addActionListener(e -> {
                // Caso seja um número, ele é digitado no visor
                if ("0123456789".contains(t)) {
                    if (novoNumero || visor.getText().equals("0")) {
                        visor.setText(t); // Substitui o valor atual por esse número
                    } else {
                        visor.setText(visor.getText() + t); // Acrescenta o número ao visor
                    }
                    novoNumero = false; // Agora o usuário está digitando parte de um número
                }
                // Caso seja um operador matemático (+, -, *, /)
                else if (t.equals("+") || t.equals("-") || t.equals("*") || t.equals("/")) {
                    if (visor.getText().equals("Erro")) {
                        return; // Se aparecer erro, a operação não continua
                    }

                    // Quando o usuário já digitou um número e depois escolhe outro operador,
                    // a calculadora conclui a conta anterior antes de guardar o novo operador.
                    // Isso permite encadear operações, como: 2 + 3 + 4.
                    // O !novoNumero evita que o programa calcule quando o usuário clica dois
                    // operadores seguidos.
                    if (!operador.isEmpty() && !novoNumero) {
                        calcular(visor);
                        if (visor.getText().equals("Erro")) {
                            return; // Se houve divisão por zero, interrompe a operação atual
                        }
                    }

                    valorAnterior = Double.parseDouble(visor.getText()); // Guarda o valor atual antes da operação
                    operador = t; // Guarda qual operação será realizada
                    novoNumero = true; // Quando o próximo botão for pressionado, começa um novo número
                }
                // Caso seja o botão de igual (=)
                else if (t.equals("=")) {
                    if (!operador.isEmpty()) {
                        calcular(visor); // Chama o método que executa a conta e atualiza o visor
                        operador = ""; // Limpa o operador após a conta ser concluída
                    }
                }
                // Caso seja o botão de ponto decimal
                else if (t.equals(".")) {
                    if (novoNumero) {
                        visor.setText("0."); // Se não houver número, começa com 0.
                        novoNumero = false;
                    } else if (!visor.getText().contains(".")) {
                        visor.setText(visor.getText() + "."); // Adiciona o ponto se ainda não existir
                    }
                }

                else if (t.equals("CE")) {
                    visor.setText("0");
                    valorAnterior = 0;
                    operador = "";
                    novoNumero = true;
                }

                else if (t.equals("back")) {
                    if (novoNumero) {
                        return;
                    }
                    String texto = visor.getText();
                    if (texto.length() > 1 && !(texto.startsWith("-") && texto.length() == 2)) {
                        visor.setText(texto.substring(0, texto.length() - 1));
                    } else{
                         visor.setText("0");
                    }
                }

                else if (t.equals("+/-")) {
                    String texto = visor.getText();
                    if (texto.equals("0") || texto.equals("Erro")) {
                        return;
                    }
                    if (texto.startsWith("-")) {
                        visor.setText(texto.substring(1));
                    } else{
                        visor.setText("-" + texto);
                    }
                }

                // Caso seja o botão %, que calcula uma porcentagem
                else if (t.equals("%")) {
                    if (visor.getText().equals("Erro")) {
                        return;
                    }
                    double atual = Double.parseDouble(visor.getText());

                    if (operador.equals("+") || operador.equals("-")) {
                        atual = valorAnterior * atual / 100; // Porcentagem do valor anterior
                    } else {
                        atual = atual / 100; // Sem operação, ou com * e /: divide por 100
                    }

                    visor.setText(formatar(atual));
                    novoNumero = operador.isEmpty(); // Sem conta pendente, o valor está "fechado"
                }
            });

            painel.add(botao); // Adiciona o botão ao painel da calculadora
        }

        // Coloca o painel de botões no centro da janela
        janela.add(painel, BorderLayout.CENTER);

        // Exibe a janela na tela
        janela.setVisible(true);
    }

    // Este método executa a conta atual usando o valor anterior, o operador
    // escolhido
    // e o valor digitado agora. Ele é usado quando o usuário clica em "=" ou quando
    // ele escolhe outro operador para continuar uma sequência de cálculos.
    private static void calcular(JTextField visor) {
        double atual = Double.parseDouble(visor.getText()); // Número digitado no momento da operação
        double resultado = 0;


        // Escolhe a operação matemática correta conforme o operador guardado
        switch (operador) {
            case "+":
                resultado = valorAnterior + atual;
                break;
            case "-":
                resultado = valorAnterior - atual;
                break;
            case "*":
                resultado = valorAnterior * atual;
                break;
            case "/":
                if (atual == 0) {
                    visor.setText("Erro"); // Evita divisão por zero, que não existe matematicamente
                    operador = "";
                    novoNumero = true;
                    return; // Sai do método imediatamente para não processar uma conta inválida
                }
                resultado = valorAnterior / atual;
                break;
            default:
                break;
        }

        // Formata o resultado para evitar muita casa decimal e mostrar um valor mais
        // limpo
        visor.setText(formatar(resultado));

        valorAnterior = resultado; // Guarda o resultado para permitir cálculos em sequência
        novoNumero = true;
    }

    // Formata um número para o visor: limita as casas decimais e remove zeros inúteis
    private static String formatar(double valor) {
        return new BigDecimal(valor)
                .setScale(5, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
}