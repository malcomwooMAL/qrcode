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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class QRCodeScanner implements Runnable, ThreadFactory {

    private final Executor executor = Executors.newSingleThreadExecutor(this);
    private Webcam webcam = null;

    public QRCodeScanner() {
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

        JFrame window = new JFrame("Leitor de QR Code");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);

        // Inicia a thread para a leitura do QR code.
        executor.execute(this);
    }

    @Override
    public void run() {
        do {
            try {
                Thread.sleep(100); // Aguarda um breve momento antes de capturar o próximo frame.
            } catch (InterruptedException e) {
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
                // Se um QR code for decodificado com sucesso.
                System.out.println("QR Code detectado: " + result.getText());
                webcam.close(); // Fecha a webcam.
                System.exit(0); // Encerra a aplicação.
            }

        } while (true);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "qr-code-scanner-thread");
        t.setDaemon(true); // Define a thread como daemon para não impedir o fechamento da JVM.
        return t;
    }

    public static void main(String[] args) {
        new QRCodeScanner();
    }
}
