import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Scanner;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;

import com.sun.jna.*;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;

class vec2 {
    int x, y;
}

class Place {
    static Robot r; // still used for keyboard & clicks
    static int xorig = 245, yorig = 5, zorig = 110;
    static boolean debug = false;

    // DON'T CLICK AFTER RUNNING
    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(new File("output.fwire"));
        try {
            r = new Robot();
            Thread.sleep(10000);
            if (!debug) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    String[] numbers = line.split(" ");
                    int x = Integer.parseInt(numbers[0]);
                    int y = Integer.parseInt(numbers[1]);
                    int z = Integer.parseInt(numbers[2]);
                    int tex1 = Integer.parseInt(numbers[3]);
                    float tex2 = Float.parseFloat(numbers[4]);
                    placePartDupe(x, y, z, tex1, tex2, false, 255, 100, 100);
                }
            } else {
                printNotification();
                Thread.sleep(8000);
                printNotification();
                Thread.sleep(8000);
                printNotification();
                Thread.sleep(8000);
                printNotification();
                Thread.sleep(8000);
                printNotification();
            }
        } catch (Exception e) {
            System.out.println("Could not init robot.");
            return;
        }

    }

    static void printNotification() {
        Point p = getPos();
        System.out.println("XY: " + p.getX() + ", " + p.getY());
        sendNotification();
    }

    static void sendNotification() {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        trayIcon.setImageAutoSize(true);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        trayIcon.displayMessage("Debug", "Captured.", TrayIcon.MessageType.INFO);
    }

    static void click(boolean once) throws InterruptedException {

        for (int i = 0; i < (once ? 1 : 2); i++) {
            r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            // Thread.sleep(80);
            r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            // Thread.sleep(120);
        }
        Thread.sleep(100); // extra time for caret to appear
    }

    static void click() throws InterruptedException {

        for (int i = 0; i < 2; i++) {
            r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            // Thread.sleep(80);
            r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            // Thread.sleep(120);
        }
        Thread.sleep(100); // extra time for caret to appear
    }

    static void press(int key) throws InterruptedException {
        r.keyPress(key);
        r.keyRelease(key);
        Thread.sleep(50);
    }

    static void moveMouse(int x, int y) throws InterruptedException {
        r.mouseMove(x, y);
        Thread.sleep(50);
    }

    static Point getPos() {
        PointerInfo p = MouseInfo.getPointerInfo();
        Point a = p.getLocation();
        return a;
    }

    static void write(String sent) throws InterruptedException {
        Thread.sleep(100);
        for (char c : sent.toCharArray()) {
            typeChar(c);
        }
        Thread.sleep(100);
    }

    private static void typeChar(char c) throws InterruptedException {
        try {
            boolean upper = Character.isUpperCase(c);
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);

            if (keyCode == KeyEvent.VK_UNDEFINED) {
                return;
            }

            if (upper) {
                r.keyPress(KeyEvent.VK_SHIFT);
            }

            r.keyPress(keyCode);
            r.keyRelease(keyCode);
            Thread.sleep(10);
            if (upper) {
                r.keyRelease(KeyEvent.VK_SHIFT);
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Cannot type character: " + c);
        }
    }

    static void clickAndWrite(int x, int y, String text) throws InterruptedException {
        r.mouseMove(x, y);
        Thread.sleep(25);
        r.mouseMove(x + 3, y); // tiny nudge
        Thread.sleep(25);
        r.mouseMove(x, y);
        // Thread.sleep(50);
        // Thread.sleep(30);
        click();
        // Thread.sleep(30);
        write(text);
        // Thread.sleep(30);
        press(KeyEvent.VK_ENTER);
        // Thread.sleep(200);
    }

    static void safeClickAt(int x, int y) throws InterruptedException {

        r.mouseMove(x, y);
        // Thread.sleep(1000);
        Thread.sleep(25);
        r.mouseMove(x + 3, y);
        Thread.sleep(25);
        r.mouseMove(x, y);
        // Thread.sleep(2500);
        click();
        // hread.sleep(1000);

    }

    static void safeClickAt(int x, int y, boolean once) throws InterruptedException {

        r.mouseMove(x, y);
        // Thread.sleep(1000);
        Thread.sleep(25);
        r.mouseMove(x + 3, y);
        Thread.sleep(25);
        r.mouseMove(x, y);
        // Thread.sleep(2500);
        click(once);
        // Thread.sleep(1000);

    }

    static void safeDragClickAt(int startX, int startY) throws InterruptedException {
        HWND hwnd = User32.INSTANCE.FindWindowA(null, "Roblox");
        User32.INSTANCE.SetForegroundWindow(hwnd);
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

        // Convert to 0-65535 absolute coordinates
        int sx = startX * 65535 / screenWidth;
        int sy = startY * 65535 / screenHeight;

        // Press down at start
        User32.INPUT down = new User32.INPUT();
        down.type = 0;
        down.mi = new User32.MOUSEINPUT();
        down.mi.dx = sx;
        down.mi.dy = sy;
        down.mi.dwFlags = User32.MOUSEEVENTF_LEFTDOWN | User32.MOUSEEVENTF_MOVE | User32.MOUSEEVENTF_ABSOLUTE;
        User32.INSTANCE.SendInput(1, new User32.INPUT[] { down }, down.size());

        // Thread.sleep(200);

        // Nudge +1 pixel right
        for (int i = 0; i < 5; i++) {
            startX++;
            sx = startX * 65535 / screenWidth;
            sy = startY * 65535 / screenHeight;
            User32.INPUT moveRight = new User32.INPUT();
            moveRight.type = 0;
            moveRight.mi = new User32.MOUSEINPUT();
            moveRight.mi.dx = sx;
            moveRight.mi.dy = sy;
            moveRight.mi.dwFlags = User32.MOUSEEVENTF_MOVE | User32.MOUSEEVENTF_ABSOLUTE;
            User32.INSTANCE.SendInput(1, new User32.INPUT[] { moveRight }, moveRight.size());
            Thread.sleep(10);
        }
        Thread.sleep(10);

        // Nudge -1 pixel left
        for (int i = 0; i < 5; i++) {
            startX--;
            sx = startX * 65535 / screenWidth;
            sy = startY * 65535 / screenHeight;
            User32.INPUT moveLeft = new User32.INPUT();
            moveLeft.type = 0;
            moveLeft.mi = new User32.MOUSEINPUT();
            moveLeft.mi.dx = sx;
            moveLeft.mi.dy = sy;
            moveLeft.mi.dwFlags = User32.MOUSEEVENTF_MOVE | User32.MOUSEEVENTF_ABSOLUTE;
            User32.INSTANCE.SendInput(1, new User32.INPUT[] { moveLeft }, moveLeft.size());
            Thread.sleep(10);
        }
        Thread.sleep(10);

        // Release mouse
        User32.INPUT up = new User32.INPUT();
        up.type = 0;
        up.mi = new User32.MOUSEINPUT();
        up.mi.dx = sx;
        up.mi.dy = sy;
        up.mi.dwFlags = User32.MOUSEEVENTF_LEFTUP | User32.MOUSEEVENTF_MOVE | User32.MOUSEEVENTF_ABSOLUTE;
        User32.INSTANCE.SendInput(1, new User32.INPUT[] { up }, up.size());
    }

    static void colorSelect(int r, int g, int b) throws InterruptedException {
        safeClickAt(14, 690, true);
        float[] hsb = Color.RGBtoHSB(r, g, b, null);

        float hue = hsb[0] * 360; // 0.0 - 1.0
        float saturation = hsb[1] * 100; // 0.0 - 1.0
        float brightness = hsb[2] * 100;
        System.out.println("HSB: " + hue + " " + saturation + " " + brightness);

        // hue is left right
        // saturation in up down
        // 847 , 389 TOP LEFT
        // 1052, 389 TOP RIGHT
        // 1052, 580 BOTTOM RIGHT
        // 847 , 580 BOTTOM LEFT
        float x = 1052 - 847;
        float y = 580 - 389;

        x /= 360;
        y /= 100;
        int xs = 847 + (int) (hue * x);
        int ys = 389 + (int) (y * saturation);
        for (int i = 0; i < 7; i++) {
            safeDragClickAt(xs, ys);
        }
        // Thread.sleep(100000);

    }

    static int lx = 0, ly = 0, lz = 0;
    // static int lr = 0, lg = 0, lb = 0;
    static int ltex1 = 0;
    static float ltex2 = 0;
    static boolean removedLastTex = false;

    static void placePartDupe(int x, int y, int z, int tex1, float tex2, boolean basicColor, int red, int green,
            int blue)
            throws InterruptedException {
        x += xorig;
        y += yorig;
        z += zorig;

        System.out.println(x + " " + y + " " + z);
        r.keyPress(KeyEvent.VK_SHIFT);
        r.keyPress(KeyEvent.VK_C);
        Thread.sleep(100);
        r.keyRelease(KeyEvent.VK_C);
        r.keyRelease(KeyEvent.VK_SHIFT);
        Thread.sleep(100);

        press(KeyEvent.VK_Z);

        if (lx != x)
            clickAndWrite(100, 705, Integer.toString(x));
        if (ly != y)
            clickAndWrite(150, 705, Integer.toString(y));
        if (lz != z)
            clickAndWrite(200, 705, Integer.toString(z));

        press(KeyEvent.VK_G);
        if (removedLastTex && tex1 != 0) {
            // click add textures
            safeClickAt(109, 521);

            // set -500
            clickAndWrite(112, 556, Integer.toString(-500));
            removedLastTex = false;

            if (ltex1 != tex1)
                clickAndWrite(84, 590, Integer.toString(tex1));
            if (ltex2 != tex2)
                clickAndWrite(130, 590, Float.toString(tex2));
        } else {
            if (ltex1 != tex1)
                clickAndWrite(84, 590, Integer.toString(tex1));
            if (ltex2 != tex2)
                clickAndWrite(130, 590, Float.toString(tex2));
        }
        if (tex1 == 0) {
            safeClickAt(108, 623);
            removedLastTex = true;
        }
        press(KeyEvent.VK_V);
        if (basicColor) {
            safeClickAt(13, 600);
        } else {
            colorSelect(red, green, blue);
            safeClickAt(913, 727, true);
            // clickAndWrite(926, 614, Integer.toString((int) hue));
            // clickAndWrite(969, 653, Integer.toString((int) saturation));
            // clickAndWrite(968, 686, Integer.toString((int) brightness));
        }

        lx = x;
        ly = y;
        lz = z;
        ltex1 = tex1;
        ltex2 = tex2;
        /*
         * if (!basicColor) {
         * //lr = red;
         * //lg = green;
         * //lb = blue;
         * } else {
         * lr = -1;
         * lg = -1;
         * lb = -1;
         * }
         */

        // press(KeyEvent.VK_ENTER);

        // --- Final enter (if needed) ---
        // press(KeyEvent.VK_ENTER);
        // Thread.sleep(200);
    }

    // --- JNA User32 wrapper ---
    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class);

        int SendInput(int nInputs, INPUT[] pInputs, int cbSize);

        int MOUSEEVENTF_MOVE = 0x0001;
        int MOUSEEVENTF_ABSOLUTE = 0x8000;
        int MOUSEEVENTF_LEFTDOWN = 0x0002;
        int MOUSEEVENTF_LEFTUP = 0x0004;

        class INPUT extends Structure {
            public static class ByReference extends INPUT implements Structure.ByReference {
            }

            public static class ByValue extends INPUT implements Structure.ByValue {
            }

            public int type; // 0 = INPUT_MOUSE
            public MOUSEINPUT mi;

            @Override
            protected List<String> getFieldOrder() {
                return Arrays.asList("type", "mi");
            }
        }

        class MOUSEINPUT extends Structure {
            public int dx, dy;
            public int mouseData;
            public int dwFlags;
            public int time;
            public BaseTSD.ULONG_PTR dwExtraInfo;

            @Override
            protected List<String> getFieldOrder() {
                return Arrays.asList("dx", "dy", "mouseData", "dwFlags", "time", "dwExtraInfo");
            }
        }

        boolean SetForegroundWindow(HWND hWnd);

        HWND FindWindowA(String lpClassName, String lpWindowName);
    }

}
