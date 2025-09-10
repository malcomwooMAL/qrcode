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

public class QRCodeScannerImperative {

    private Webcam webcam = null;

    public QRCodeScannerImperative() { //construtor não recebe parâmetros
        // Inicializa a webcam com uma resolução padrão.
        Dimension size = WebcamResolution.VGA.getSize();
        webcam = Webcam.getWebcams().get(0); // Usa a primeira webcam encontrada
        webcam.setViewSize(size);
        webcam.open();
        
        // Cria o painel da webcam e a janela para exibição.
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);

        JFrame window = new JFrame("Leitor de QR Code (Imperativo)");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);

        // Instancia e inicia a thread de escaneamento explicitamente.
        ScannerThread scannerThread = new ScannerThread(webcam);
        scannerThread.start();
    }

    /**
     * Classe interna que herda de Thread para encapsular a lógica de escaneamento.
     * Esta é uma abordagem imperativa para o gerenciamento de threads.
     */
    private static class ScannerThread extends Thread {
        
        private final Webcam webcam;

        public ScannerThread(Webcam webcam) {
            this.webcam = webcam;
            this.setName("qr-code-scanner-thread");
            this.setDaemon(true); // Garante que a thread não impeça o fim da aplicação.
        }

        @Override
        public void run() {
            do {
                try {
                    // Aguarda um breve momento antes de capturar o próximo frame.
                    Thread.sleep(100); 
                } catch (InterruptedException e) {
                    System.err.print("Thread foi interrompido.\n");
                    e.printStackTrace();
                }

                Result result = null;
                BufferedImage image = null;

                if (webcam.isOpen()) {
                    if ((image = webcam.getImage()) == null) {
                        continue; // Pula para a próxima iteração se a imagem não estiver pronta.
                    }

                    // Converte a imagem capturada para um formato que o ZXing possa processar.
                    LuminanceSource source = new BufferedImageLuminanceSource(image);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    try {
                        // Tenta decodificar a imagem.
                        result = new MultiFormatReader().decode(bitmap);
                    } catch (NotFoundException e) {
                        // Nenhuma exceção é lançada, pois é normal não encontrar um QR code em cada frame.
                    }
                }

                if (result != null) {
                    // Exibe o resultado diretamente no console.
                    System.out.println("QR Code detectado: " + result.getText());
                    try {
                        // Adiciona uma pausa para evitar leituras excessivamente rápidas.
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        System.err.print("Thread foi interrompido.\n");
                        e.printStackTrace();
                    }
                }

            } while (true);
        }
    }

    public static void main(String[] args) {
        new QRCodeScannerImperative();
    }
}

