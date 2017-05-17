package br.com.msfu.utils.textfield;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;

/**
 *
 * @author c90
 */
public class JCrepzTextField extends JTextField implements MouseMotionListener, FocusListener, CrepzBuscador {

    private DefaultListModel listModel;
    private JList jList;
    private int linCont;
    private JScrollPane scrollArea;
    private boolean dicaVisivel;
    private boolean mouseIsOnList;
    private CrepzBuscador crepzBuscador;

    public JCrepzTextField() {
        listModel = new DefaultListModel();
        jList = new JList();
        scrollArea = new JScrollPane();
        scrollArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jList.setVisibleRowCount(5);
        scrollArea.setViewportView(jList);
        scrollArea.setBorder(new BevelBorder(BevelBorder.RAISED));
        jList.addMouseListener(listMouseClicked);
        jList.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addKeyListener(textAreaKeyEvent);
        addFocusListener(this);
        jList.addMouseMotionListener(this);


    }
    
    private void paintBox(ArrayList<String> lista) {
        if (lista == null || lista.isEmpty()) {
            remove();
            return ;
        }
        mouseIsOnList = false;

        linCont = -1;
        Container pane = getContentPane();
        if (!(pane.getComponent(pane.getComponentCount() - 1).equals(scrollArea) || pane.getComponent(0).equals(scrollArea))) {
            pane.add(scrollArea, pane.getComponentCount());
            //   getContentPane().setComponentZOrder(scrollArea, getContentPane().getComponentCount() - 1);
            pane.setComponentZOrder(scrollArea, 0);
            int y = this.getLocationOnScreen().y - pane.getLocationOnScreen().y;
            scrollArea.setBounds(this.getX(), y + this.getHeight(), this.getWidth(), 120);

            scrollArea.setViewportView(jList);
            ///setAllEnable(false);
            getContentPane().repaint();
            jList.clearSelection();
        }
        jList.setModel(listModel);
        jList.setListData(lista.toArray());

        dicaVisivel = true;
        System.out.println("Mostrando");

    }
    
    private MouseAdapter listMouseClicked = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == 1) {
                if (e.getClickCount() == 2) {
                    seleciona();
                }
            }

        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == 1) {
                mouseIsOnList = true;
            }
        }
    };
    @Override
    public void mouseMoved(MouseEvent e) {
        jList.setSelectedIndex(jList.locationToIndex(e.getPoint()));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        
    }

    private KeyAdapter textAreaKeyEvent = new KeyAdapter() {

        @Override
        public void keyReleased(KeyEvent e) {
            //super.keyReleased(e);
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (jList.getSelectedIndex() == -1) {
                    if (jList.getModel().getSize() > 0) {
                        linCont++;
                        jList.setSelectedIndex(linCont);
                    }
                }
                seleciona();
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                if (linCont != jList.getModel().getSize() - 1) {
                    linCont++;
                }
                jList.setSelectedIndex(linCont);
                jList.ensureIndexIsVisible(linCont);
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                if (linCont > 0) {
                    linCont--;
                }
                jList.setSelectedIndex(linCont);
                jList.ensureIndexIsVisible(linCont);
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                remove();
                return;
            }
            if ((e.getKeyCode() < 48 || e.getKeyCode() > 95) && e.getKeyCode() != KeyEvent.VK_BACK_SPACE && e.getKeyCode() != KeyEvent.VK_DELETE) {
                return;
            }
            pesquisar(this, getText());
        }
    };

    @Override
    public void focusLost(FocusEvent e) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            ex.printStackTrace(System.err);
        }
        if (mouseIsOnList) {
            seleciona();
        } else {
            remove();
        }
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public final ArrayList<String> pesquisar(Object source, String texto) {
        if (crepzBuscador == null) {
            return null;
        }
        paintBox(crepzBuscador.pesquisar(this, texto));
        return null;
    }

    public void setCrepzBuscador(CrepzBuscador crepzBuscador) {
        this.crepzBuscador = crepzBuscador;
    }

    public boolean isDicaVisivel() {
        return dicaVisivel;
    }

    public void remove() {
//        setAllEnable(true);

        if (!isDicaVisivel()) {
            return;
        }
        System.out.println("Removendo...");
//        removeFocusListener(this);
        getContentPane().remove(scrollArea);
        getContentPane().repaint();
        dicaVisivel = false;
    }

    private void seleciona() {
        if (jList.getSelectedIndex() != -1) {
            this.setText((String) jList.getModel().getElementAt(jList.getSelectedIndex()));
        }
        remove();
    }

    private Container getContentPane() {
        return this.getRootPane();

    }

}
