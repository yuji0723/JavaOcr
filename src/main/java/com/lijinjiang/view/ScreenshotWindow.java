package com.lijinjiang.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName ScreenshotWindow
 * @Description 截图窗口
 * @Author Li
 * @Date 2022/10/21 11:26
 * @ModifyDate 2022/10/21 11:26
 * @Version 1.0
 */
public class ScreenshotWindow extends JWindow {

    private int startX, startY, endX, endY;
    private BufferedImage captureImage;
    private BufferedImage tempImage;
    private BufferedImage selectedImage; // 选择的图片
    private ToolsWindow toolsWindow; // 工具条窗口

    protected MainFrame mainFrame; // 传递过来的MainFrame

    public ScreenshotWindow(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        // 获取屏幕尺寸
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBounds(0, 0, dimension.width, dimension.height);

        // 截取屏幕
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        captureImage = robot.createScreenCapture(new Rectangle(0, 0, dimension.width, dimension.height));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 鼠标松开时记录结束点坐标，并隐藏操作窗口
                startX = e.getX();
                startY = e.getY();
                if (toolsWindow != null) {
                    toolsWindow.setVisible(false);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 鼠标松开时，显示操作窗口
                if (toolsWindow == null) {
                    toolsWindow = new ToolsWindow(ScreenshotWindow.this, e.getX(), e.getY());
                } else {
                    toolsWindow.setLocation(e.getX(), e.getY());
                }
                toolsWindow.setVisible(true);
                toolsWindow.toFront();
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                // 鼠标拖动时，记录坐标并重绘窗口
                endX = e.getX();
                endY = e.getY();

                // 临时图像，用于缓冲屏幕区域放置屏幕闪烁
                Image bufferImage = createImage(ScreenshotWindow.this.getWidth(), ScreenshotWindow.this.getHeight());
                Graphics g = bufferImage.getGraphics();
                g.drawImage(tempImage, 0, 0, null);
                int x = Math.min(startX, endX);
                int y = Math.min(startY, endY);
                int width = Math.abs(endX - startX) + 1;
                int height = Math.abs(endY - startY) + 1;
                // 加上1防止width或height0
                g.setColor(Color.BLUE);
                g.drawRect(x - 1, y - 1, width + 1, height + 1);
                // 减1加1都了防止图片矩形框覆盖掉
                selectedImage = captureImage.getSubimage(x, y, width, height);
                g.drawImage(selectedImage, x, y, null);
                ScreenshotWindow.this.getGraphics().drawImage(bufferImage, 0, 0, ScreenshotWindow.this);
            }
        });
        this.setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        RescaleOp ro = new RescaleOp(0.8f, 0, null);
        tempImage = ro.filter(captureImage, null);
        g.drawImage(tempImage, 0, 0, this);
    }

    // 保存图像到文件
    public void saveImage() throws IOException {
        // 先隐藏窗口后台执行，显得程序执行很快
        this.setVisible(false);
        toolsWindow.setVisible(false);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter()); // 不显示所有文件的下拉选
        // 文件过滤器，用户过滤可选择文件
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG", "jpg");
        fileChooser.setFileFilter(filter);

        // 初始化一个默认文件（此文件会生成到桌面上）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd_HHmmss");
        String fileName = sdf.format(new Date());
        File filePath = FileSystemView.getFileSystemView().getHomeDirectory();
        File defaultFile = new File(filePath + File.separator + fileName + ".jpg");
        fileChooser.setSelectedFile(defaultFile);

        int flag = fileChooser.showSaveDialog(this);
        if (flag == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getPath();
            // 检查文件后缀，放置用户忘记输入后缀或者输入不正确的后缀
            if (!(path.endsWith(".jpg") || path.endsWith(".JPG"))) {
                path += ".jpg";
            }
            ImageIO.write(selectedImage, "jpg", new File(path));
            dispose();
        }
    }

    // 返回截取的图片
    public void selectImage() {
        ImageIcon previewImage = new ImageIcon(selectedImage);
        this.setVisible(false);
        toolsWindow.dispose(); // 关闭工具窗口
        // 显示预览图片
        mainFrame.previewLabel.setIcon(previewImage);
        mainFrame.ocrImage = selectedImage;
    }

    /*
     * 工具窗口类
     */
    class ToolsWindow extends JWindow implements ActionListener {
        private ScreenshotWindow parentWindow;

        JButton saveBtn, cancelBtn, selectBtn;

        public ToolsWindow(ScreenshotWindow parentWindow, int x, int y) {
            this.parentWindow = parentWindow;
            this.setLayout(new BorderLayout());
            JToolBar toolBar = new JToolBar();
            toolBar.setBorder(null); // 设置边框为空
            toolBar.setFloatable(false); // 设置不可移动

            // 保存按钮
            saveBtn = new JButton("保存");
            // 取消按钮
            cancelBtn = new JButton("✘");
            // 选定按钮
            selectBtn = new JButton("✔");

            saveBtn.addActionListener(this);
            cancelBtn.addActionListener(this);
            selectBtn.addActionListener(this);

            toolBar.add(saveBtn);
            toolBar.add(cancelBtn);
            toolBar.add(selectBtn);

            this.add(toolBar, BorderLayout.NORTH);

            this.setLocation(x, y);
            this.pack();
            this.setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            if (e.getSource() == saveBtn) {
                try {
                    parentWindow.saveImage();
                    dispose();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (e.getSource() == cancelBtn) {
                parentWindow.dispose();
                dispose();
            }
            if (e.getSource() == selectBtn) {
                // 返回选定的图片
                parentWindow.selectImage();
                dispose();
            }
        }
    }
}
