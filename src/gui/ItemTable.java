package gui;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import json.Item;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;


/**
 * Creates a table view of all profitable items to flip on the flea.
 */
public class ItemTable extends JFrame implements NativeKeyListener, WindowListener {

    /**
     * The system's clipboard.
     */
    public static final Clipboard SYSTEM_CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

    /**
     * The scroll pane containing our beautiful table.
     */
    private JScrollPane myJScrollPane;

    /**
     * A table of items.
     */
    private JTable myJTable;

    /**
     * Most of the items in the game.
     */
    private Item[] myItems;

    /**
     * Current row selection.
     */
    private int myRow;

    /**
     * Creates a gui containing a table of many items.
     * @throws IOException
     * @throws InterruptedException
     */
    public ItemTable() throws IOException, InterruptedException {
        GlobalScreen.setEventDispatcher(new SwingDispatchService());
        setTitle("FleaFlip");
        setSize(500, 750);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(this);
        this.add(createItemTable());
        setVisible(true);

    }

    /**
     * Creates the item table and scroll pane to go with it.
     * @return the scroll pane.
     * @throws IOException .
     * @throws InterruptedException .
     */
    private JScrollPane createItemTable() throws IOException, InterruptedException {
        myItems = Item.getItems();
        TableModel dataModel = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return myItems.length;
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getValueAt(final int rowIndex, final int columnIndex) {
                return myItems[rowIndex];
            }

            @Override
            public String getColumnName(int columnIndex){
                return "Item Name :  Price Difference (Optimal Vendor)";
            }
        };

        myJTable = new JTable(dataModel);
        myJTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        removeBinds(myJTable);
        myJScrollPane = new JScrollPane(myJTable);
        removeBinds(myJScrollPane);
        myRow = 0;
        selectRow(myRow);
        return myJScrollPane;
    }

    /**
     * Removes the up and down arrow binds from a component.
     * @param theJComponent the component to alter.
     */
    private void removeBinds(JComponent theJComponent){
        InputMap im = theJComponent.getInputMap();
        im.put(KeyStroke.getKeyStroke("DOWN"), "none");
        im.put(KeyStroke.getKeyStroke("UP"), "none");
        InputMap im2 = theJComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im2.put(KeyStroke.getKeyStroke("DOWN"), "none");
        im2.put(KeyStroke.getKeyStroke("UP"), "none");
        InputMap im3 = theJComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im3.put(KeyStroke.getKeyStroke("DOWN"), "none");
        im3.put(KeyStroke.getKeyStroke("UP"), "none");
    }

    /**
     * Selects a row.
     * @param theRow the row to select.
     */
    public void selectRow(int theRow) {
        myJTable.setRowSelectionInterval(theRow, theRow);
    }

    /**
     * Sets the system's clipboard to a string.
     * @param theString the string.
     */
    public static void setClipboard(final String theString) {
        // Solution from https://stackoverflow.com/questions/3591945/copying-to-the-clipboard-in-java
        StringSelection selection = new StringSelection(theString);
        SYSTEM_CLIPBOARD.setContents(selection, selection);
    }


    @Override
    public void windowOpened(final WindowEvent e) {
        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            ex.printStackTrace();

            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(this);
    }

    @Override
    public void windowClosing(final WindowEvent e) {

    }

    @Override
    public void windowClosed(final WindowEvent e) {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException theE) {
            theE.printStackTrace();
        }
        System.exit(0);
    }

    @Override
    public void windowIconified(final WindowEvent e) {

    }

    @Override
    public void windowDeiconified(final WindowEvent e) {

    }

    @Override
    public void windowActivated(final WindowEvent e) {

    }

    @Override
    public void windowDeactivated(final WindowEvent e) {

    }

    @Override
    public void nativeKeyTyped(final NativeKeyEvent nativeEvent) {
        //
    }

    /**
     * Global keybinds. Up and down select rows, left copies the name, and right copies the vendor price.
     * @param nativeEvent The key that is currently being pressed.
     */
    @Override
    public void nativeKeyPressed(final NativeKeyEvent nativeEvent) {
        int selectedRow = myJTable.getSelectedRow();
        switch(nativeEvent.getKeyCode()) {
            case NativeKeyEvent.VC_DOWN -> rowDown();
            case NativeKeyEvent.VC_UP -> rowUp();
            case NativeKeyEvent.VC_LEFT -> setClipboard(myItems[selectedRow].getName());
            case NativeKeyEvent.VC_RIGHT -> setClipboard(Integer.toString(myItems[selectedRow].getVendorPrice()));
            default -> doNothing();
        }
    }

    /**
     * Moves the selection a row up.
     */
    private void rowUp() {
        if (myRow > 0) {
            myRow = myJTable.getSelectedRow() - 1;
            try {
                selectRow(myRow);
            } catch (Exception e) {
                doNothing();
            }
        }
    }

    /**
     * Moves the selection a row down.
     */
    private void rowDown() {
        if (myRow < myItems.length) {
            myRow = myJTable.getSelectedRow() + 1;
        }
        try {
            selectRow(myRow);
        } catch (Exception e) {
            doNothing();
        }
    }

    /**
     * Does nothing.
     */
    private void doNothing(){
        // Does nothing.
    }

    @Override
    public void nativeKeyReleased(final NativeKeyEvent nativeEvent) {
        //
    }
}
