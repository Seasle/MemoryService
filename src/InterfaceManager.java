import javax.sound.sampled.LineEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import java.util.logging.Level;

public class InterfaceManager {
    private static final InterfaceManager interfaceManager = new InterfaceManager();

    private final Logger logger = LoggerProvider.getInstance();
    private final String title = "Интерфейс";
    private JFrame frame = null;

    public InterfaceManager() {
        this.initTray();
        this.initInterface();
    }

    private void initTray() {
        SystemTray tray = SystemTray.getSystemTray();
        try {
            ImageIcon icon = new ImageIcon("disk_16.png");
            TrayIcon trayIcon = new TrayIcon(icon.getImage());

            PopupMenu popupMenu = new PopupMenu();
            MenuItem openItem = new MenuItem("Открыть");
            MenuItem exitItem = new MenuItem("Выход");

            openItem.addActionListener(event -> {
                this.showInterface();
            });

            exitItem.addActionListener(event -> {
                System.exit(0);
            });

            popupMenu.add(openItem);
            popupMenu.add(exitItem);

            trayIcon.setToolTip(this.title);
            trayIcon.setPopupMenu(popupMenu);

            tray.add(trayIcon);

            logger.log(Level.INFO, "Tray icon has been successfully created.");
        } catch (AWTException exception) {
            logger.log(Level.WARNING, exception.getMessage());
        }
    }

    private void initInterface() {
        Interface form = new Interface();
        ImageIcon icon = new ImageIcon("disk_32.png");

        this.frame = new JFrame(this.title);

        this.frame.setIconImage(icon.getImage());
        this.frame.setContentPane(form.contentPane);
        this.frame.setSize(600, 400);
        this.frame.setLocationRelativeTo(null);
        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                super.windowClosing(event);

                logger.log(Level.INFO, "Interface has been closed.");
            }
        });

        logger.log(Level.INFO, "Interface has been successfully created.");
    }

    public void showInterface() {
        this.frame.setVisible(true);

        logger.log(Level.INFO, "Interface has been opened.");
    }

    public static InterfaceManager getInstance() {
        return interfaceManager;
    }
}