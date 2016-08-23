import java.io.*;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JMenu;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class BMPReader {

	private JFrame frmBmpReader;
	private File f;
	private JTextArea textArea;
	private String imgInfo;
	private String imgName;
	//File Header
	private byte[] formatSignature;
	private int fileSize;
	private int allZero;
	private int dataOffset;
	//Info Header
	private int headFileSize;
	private int imgWidth;
	private int imgHeight;
	private int imgPlane;
	private int bitPerPixel;
	private int zip;
	private int imgSize;
	private int HResolution;
	private int VResolution;
	private int usedColor;
	private int importColor;
	private int[][] map;
	
	private static int m_counter = 0;
	private int temp[] = new int[8];
	RandomAccessFile raf;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BMPReader window = new BMPReader();
					window.frmBmpReader.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public BMPReader() {
		initialize();
	}

	private void initialize() {
		java.awt.Dimension scr_size = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		frmBmpReader = new JFrame();
		frmBmpReader.setTitle("BMP Reader");
		frmBmpReader.setBackground(SystemColor.textHighlight);
		frmBmpReader.setBounds(100, 100, 450, 300);
		frmBmpReader.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmBmpReader.setLocation((scr_size.width - frmBmpReader.getWidth()) / 2,(scr_size.height - frmBmpReader.getHeight()) / 2);

		JMenuBar menuBar = new JMenuBar();
		frmBmpReader.setJMenuBar(menuBar);
		
		JMenu menuFile = new JMenu("File");
		
		menuBar.add(menuFile);
		
		JMenuItem menuItemOpen = new JMenuItem("Open");
		menuFile.add(menuItemOpen);
		
		JMenuItem menuItemClose = new JMenuItem("Close");
		menuFile.add(menuItemClose);
		frmBmpReader.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 238, 434, -235);
		frmBmpReader.getContentPane().add(panel);
		panel.setLayout(null);
		
		textArea = new JTextArea();
		textArea.setBackground(SystemColor.control);
		textArea.setBounds(45, 10, 352, 218);
		textArea.setEditable(false);
		frmBmpReader.getContentPane().add(textArea);
	
		menuItemOpen.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				loadImage();
			}
		});
		
		menuItemClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	}
	
	private void loadImage () {
		JFileChooser fileChooser = new JFileChooser(); 
		fileChooser.addChoosableFileFilter(new fileFilter());
		fileChooser.setCurrentDirectory(new java.io.File("."));
		fileChooser.setDialogTitle("Chooser Title");
		fileChooser.setFileSelectionMode(JFileChooser.APPROVE_OPTION);
		
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.showOpenDialog(frmBmpReader);
		
		try {
			f = fileChooser.getSelectedFile();
			imgName = f.getName();
			raf = new RandomAccessFile(f, "r");
			
			loadFileHeader();
			loadInfoHeader();
			ShowImgInfo();
			
			//init color palette
			if(usedColor == 0){
				usedColor = (int)Math.pow(2, bitPerPixel);
			}
			int palette[] = new int[usedColor];
			
			switch(bitPerPixel){
			case 1 :
			case 4 :
			case 8 :
				palette = loadPalette(usedColor);
				ReadRowData(palette);
				break;
			case 24:
				Read24bit();
				break;
			}
			
			showPicture();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void loadFileHeader() {
		try {
			
			formatSignature = new byte[2];
			formatSignature = read2Byte (formatSignature,raf);
			
			fileSize = read4Byte();
			allZero = read4Byte();
			dataOffset = read4Byte();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void loadInfoHeader(){
		try{
			
			headFileSize = read4Byte();
			imgWidth     = read4Byte();
			imgHeight    = read4Byte();
			imgPlane     = read2Byte();
			bitPerPixel  = read2Byte();
			zip          = read4Byte();
			imgSize      = read4Byte();
			HResolution  = read4Byte();
			VResolution  = read4Byte();
			usedColor    = read4Byte();
			importColor  = read4Byte();
			
		}catch(Exception e ){
			e.printStackTrace();
		}
	}
	
	private int[] loadPalette(int colorNum){
		
		int red ,green, blue,alpha;
		int[] palette;
		palette = new int[colorNum];
		
		for (int i = 0 ; i < colorNum ; i++){
			
			blue  = getColorValue();
			green = getColorValue();
			red   = getColorValue();
			alpha = getColorValue();
			
			Color c = new Color(red,green,blue,alpha);
			palette[i] = c.getRGB();
		}
		return palette;
	}
	
	private int read4Byte(){
		
		byte[] temp = new byte[4];
		try{
			for (int i = 0 ; i < 4 ; i++){
				temp[i] = raf.readByte();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return transToInt(temp);
	}
	
	private int read2Byte(){
		
		byte[] temp = new byte[2];
		try{
			for (int i = 0 ; i < 2 ; i++){
				temp[i] = raf.readByte();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return transToInt(temp);
	}

	private byte[] read2Byte (byte[] store,RandomAccessFile raf) {
		try {
			store[0] = raf.readByte();
			store[1] = raf.readByte();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return store;
	}
	
	private int transToInt(byte[] B){
		
		int sum=0;
		int x; 
		
		for (int i = 0 ; i < B.length ; i++){
			
			if (B[i] < 0 ){
				x = Byte.toUnsignedInt(B[i]);
			}else{
				x = B[i];
			}
			
			sum += x*(int)Math.pow(256, i);
		}
		
		return sum;
	}
	
	private int getColorValue (){
		int color = 0 ;
		try {
			color = raf.readUnsignedByte();
		}catch(Exception e){
			e.printStackTrace();
		}
		return color;
	}
	
	private void Read24bit(){
		
		int blue;
		int red;
		int green;
		try {
			map = new int[imgHeight][imgWidth];
			for (int i = imgHeight - 1 ; i >= 0 ; i--){
				for(int j = 0 ; j < imgWidth ; j++){
					
					blue = getColorValue();
					green = getColorValue();
					red = getColorValue();
					
					Color c = new Color(red,green,blue);
					map[i][j] = c.getRGB();
				}
				skip();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void ReadRowData(int[] palette){
		
		int colorIndex = 0 ; 
		try {
			map = new int[imgHeight][imgWidth];
			
			for (int i = imgHeight - 1 ; i >= 0 ; i--){
				for(int j = 0 ; j < imgWidth ; j++){
					
					//handle each pixel is color
					colorIndex = 0;
					
					for (int p = 0 ; p < bitPerPixel ; p++){
						colorIndex += readOne()*(int)Math.pow(2,bitPerPixel-p-1);
					}
					
					map[i][j] = palette[colorIndex];
				}
				
				skip();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void showPicture(){
		BufferedImage im = new BufferedImage(imgWidth,imgHeight,BufferedImage.TYPE_INT_RGB);
		try {
			for (int i =  0; i <  imgHeight; i++){
				for(int j = 0; j < imgWidth; j++){
					im.setRGB(j, i, map[i][j]);
				}
			}
			
			java.awt.Dimension scr_size = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			JFrame fr = new JFrame();
			fr.setTitle("Show picture");
			fr.setSize(imgWidth+100,imgHeight+100);
			fr.setLocation((scr_size.width - fr.getWidth()) /2 ,(scr_size.height - fr.getHeight()) / 2);
			fr.getContentPane().add(new JLabel(new ImageIcon(im)));

			fr.setVisible(true);

		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private int readOne(){
		int  x; 
		
		try {
			if(m_counter % 8 == 0){
				m_counter = 0 ;
				x = raf.readUnsignedByte();
				
				for (int i = 7 ; i >= 0; i--){
					temp[i] = x%2;
					x = x/2;
				}
			}
			m_counter++;
			return temp[m_counter-1];
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
	}
		
	private void skip(){
		
		int x = (int)Math.ceil((imgWidth*bitPerPixel)/8.0);
		if (x%4 != 0 )
			x = 4-x%4;
		else 
			x = 0;

		m_counter = 0 ;
		
		for (int k = 0 ; k < x; k++){
			try{
				raf.readUnsignedByte();
			}catch(Exception e){};
		}
	}
	
	private void ShowImgInfo (){
	
		imgInfo = ""+allZero ;
		imgInfo = "";
		imgInfo += "檔案名稱      : " +imgName+"\n";
		imgInfo += "檔案大小      : "+fileSize+" Byte"+"\n";
		imgInfo += "資料偏移量  : "+dataOffset+" Byte"+"\n";
		imgInfo += "標頭大小      : "+headFileSize+" Byte"+"\n";
		imgInfo += "圖片大小      : "+imgWidth+"x"+imgHeight+"\n";
		imgInfo += "imgPlane     : "+imgPlane+"\n";
		imgInfo += "位元/像素     : "+bitPerPixel+" bit"+"\n";
		imgInfo += "壓縮方式      : ";
		
		switch(zip){
		case 0 :
			imgInfo += "None (BI_RGB), no pallet"+"\n";
			break;
		case 1 :
			imgInfo += "RLE 8bpp (BI_RLE8)"+"\n";
			break;
		case 2 :
			imgInfo += "RLE 4bpp (BI_RLE4)"+"\n";
			break;
		case 3:
			imgInfo += "Bitfileds (BI_BITFIELDS)";
			break;
		}
		
		imgInfo += "檔案大小      : "+imgSize+"\n";
		imgInfo += "水平解析度  : "+HResolution+"\n";
		imgInfo += "垂直解析度  : "+VResolution+"\n";
		imgInfo += "使用顏色數  : "+usedColor+"\n";
		imgInfo += "重要顏色數  : "+importColor+"\n";
		textArea.setText(imgInfo);;
	}

	class fileFilter extends FileFilter
	{
		public boolean accept(File file) {
			int i = file.getName().lastIndexOf('.');
			if (file.isDirectory())
				return true;
			
			if (i == -1)
				return false;
			
			String filePath = file.getName().substring(i).toLowerCase();
			if (filePath.equals(".bmp"))
				return true;
			return false;
		}

		public String getDescription() {
			return "*.bmp";
		}
	}
}


