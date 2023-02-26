package gui;

import javax.swing.*;
import java.io.IOException;

public class Main {

    private Main() { }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            try {
                new ItemTable();
            } catch (IOException theE) {
                theE.printStackTrace();
            } catch (InterruptedException theE) {
                theE.printStackTrace();
            }
        });
    }
}
