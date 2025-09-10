package edu.senai;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Classe encapsulada para ler um QR code da webcam.
 * Expõe um único método público para obter os dados do QR code.
 */
public class QRCodeReader {

    private Webcam webcam = null;
    private final BlockingQueue<String> resultQueue = new ArrayBlockingQueue<>(1);

    /**
     * O construtor é público para permitir a instanciação, mas toda a lógica
     * complexa é gerenciada internamente e de forma privada.
     */
    public QRCodeReader() {
        initializeWebcam();
    }

    /**
     * Método público de interface. Aguarda até que um QR code seja lido
     * e retorna seu conteúdo como uma String.
     * * @return O texto contido no QR code.
     * @throws InterruptedException se a thread for interrompida enquanto aguarda.
     */
    public String readQRCode() throws InterruptedException {
        // O método take() bloqueia a execução até que um item esteja disponível na fila.
        String result = resultQueue.take();
        closeWebcam(); // Fecha a webcam após uma leitura bem-sucedida.
        return result;
    }

    /**
     * Inicializa a webcam, a interface gráfica e a thread de escaneamento.
     * Este método é privado para encapsular os detalhes de implementação.
     */
    private void initializeWebcam() {
        Dimension size = WebcamResolution.VGA.getSize();
        webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new IllegalStateException("Nenhuma webcam encontrada.");
        }
        webcam.setViewSize(size);
        webcam.open();

        // A janela de visualização é importante para o usuário alinhar o QR code.
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setMirrored(true);

        JFrame window = new JFrame("Aponte o QR Code para a Câmera");
        window.add(panel);
        window.setResizable(true);
        // Garante que o fechamento da janela não encerre a aplicação inteira.
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        window.pack();
        window.setVisible(true);

        // Inicia a thread de escaneamento.
        ScannerThread scannerThread = new ScannerThread(webcam, resultQueue, window);
        scannerThread.start();
    }

    /**
     * Fecha a webcam de forma segura.
     */
    private void closeWebcam() {
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    /**
     * Classe interna que gerencia a thread de escaneamento de forma isolada.
     */
    private static class ScannerThread extends Thread {
        private final Webcam webcam;
        private final BlockingQueue<String> queue;
        private final JFrame scannerWindow;

        public ScannerThread(Webcam webcam, BlockingQueue<String> queue, JFrame scannerWindow) {
            this.webcam = webcam;
            this.queue = queue;
            this.scannerWindow = scannerWindow;
            this.setName("qr-code-scanner-thread");
            this.setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                BufferedImage image;
                if (!webcam.isOpen() || (image = webcam.getImage()) == null) {
                    continue;
                }

                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                try {
                    Result result = new MultiFormatReader().decode(bitmap);
                    if (result != null) {
                        // Coloca o resultado na fila para ser consumido pelo método público.
                        queue.put(result.getText());
                        scannerWindow.dispose(); // Fecha a janela de visualização.
                        break; // Encerra a thread após encontrar o resultado.
                    }
                } catch (NotFoundException e) {
                    // Continua o loop.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Método main para demonstração de uso da classe QRCodeReader.
     */
    public static void main(String[] args) {
        System.out.println("Iniciando leitor de QR Code...");
        
        
        //tentativa da primeira leitura
        QRCodeReader reader = new QRCodeReader();
        try {
            String data = reader.readQRCode();
            System.out.println("Leitura bem-sucedida!");
            System.out.println("Dados do QR Code: " + data);
        } catch (InterruptedException e) {
            System.err.println("A operação de leitura foi interrompida.");
            Thread.currentThread().interrupt();
        }        
        

        //tentativa da segunda leitura
        QRCodeReader reader2 = new QRCodeReader();
        try {
            String data = reader2.readQRCode();
            System.out.println("Leitura bem-sucedida!");
            System.out.println("Dados do QR Code: " + data);
        } catch (InterruptedException e) {
            System.err.println("A operação de leitura foi interrompida.");
            Thread.currentThread().interrupt();
        } 

    }
}

