/*/////////////////////////////////////////////////////////////////////////////////////
CalSD permite calcular sonidos diferenciales entre dos notas o armónicos de dos notas.
Muestra la frecuencia de ambos sonidos y también su diferencia, mostrando su afinación
con respecto al piano con un indicador rectangular que muestra un corrimiento con
respecto a las teclas en todos aquellos casos en que el error de afinación sea superior
al 1%.
/////////////////////////////////////////////////////////////////////////////////////*/
import javax.swing.JComponent;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLayeredPane;
import javax.swing.JSlider;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.Soundbank;
import javax.sound.midi.MidiUnavailableException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.Math;
import java.text.DecimalFormat;
import java.net.URL;

/*/////////////////////////////////////////////////////////////////////////////////////
La clase interfaz construye la ventana del programa y contiene todos los métodos para
su funcionamiento.
/////////////////////////////////////////////////////////////////////////////////////*/
class Interfaz implements ActionListener {

	//Variables globales
	Color negro = new Color(45, 45, 45);
	Color blanco = new Color(255, 255, 255);
	Color rojo = new Color(190, 40, 40);
	Color azul = new Color(90, 140, 190);
	Color verde = new Color(40, 130, 15);
	JTextField primeraNota = new JTextField("");
	JTextField segundaNota = new JTextField("");
	JTextField diferencial = new JTextField("");
	JButton bNotas[][] = new JButton[9][12];
	JLabel octava[] = new JLabel[9];
	JLabel soDif = new JLabel();
	JButton borrar = new JButton();
	JButton verMemorias = new JButton();
	JSlider slider[] = new JSlider[2];
	int posicionMemoria = 0;
	int posicionMemoriaVer = 0;
	int contadorEjecutar = 0;
	int historial[][] = new int[12][6];
	String idioma[] = new String[8];
	boolean hayInstrumentos = true;
	boolean mostrandoMemoria = false;
	Font classInfoFont;
	Font dataObtenidaFont;
	Synthesizer sint;
	MidiChannel canal;
	boolean esPrimeraNota = true;
	double doCero = 16.351598;
	double frecuenciaNota = 0.0;
	double f1 = 0;
	double f2 = 0;
	double dif = 0;
	DecimalFormat z = new DecimalFormat("#,###,###.00");
	
	/*//////////
	Constructor
	/////////*/
	Interfaz(){
		
		//Construcción del sintetizador
		try {
			sint = MidiSystem.getSynthesizer();
			sint.open();
         	//Confirmación de la existencia de un banco de sonidos.
         	if (sint.getDefaultSoundbank().getInstruments() == null){
         		hayInstrumentos = false;
         	} else {         		
				canal = sint.getChannels()[0];
         	}
      	} catch (MidiUnavailableException e) {}

		getIdioma(System.getProperty("user.language"));
		
		construirVentana(System.getProperty("os.name"));
      	
	}
	
	
	/*///////////////////////////////////////////////////////
	Construcción y métodos de gestión de la interfaz gráfica.
	///////////////////////////////////////////////////////*/
	
	//Construcción de la ventana, agregado del panel principal.
	void construirVentana(String os) {
		
		JFrame v = new JFrame("CalSD");
		v.setDefaultCloseOperation(3);
		v.setSize(935, 420);
		v.setLocationRelativeTo(null);
		v.setResizable(false);
		
		
		//Icono y tamaño de ventana para windows y linux
        if (os.startsWith("Windows") || os.startsWith("Linux")){
	        URL iconoUrl = getClass().getResource("Icono.png");
			if (iconoUrl != null){
				ImageIcon icono = new ImageIcon(iconoUrl);
				v.setIconImage(icono.getImage());
			}
		} 
		
		//Definiendo formatos según sistema operativo
		if (os.startsWith("Windows")){
			classInfoFont = new Font("sansserif", Font.PLAIN, 12);
			dataObtenidaFont = new Font("monospace", Font.BOLD, 14);
		} else {
			classInfoFont = new Font("sansserif", Font.PLAIN, 12);
			dataObtenidaFont = new Font("monospace", Font.PLAIN, 14);
		}
		
		/*Inicialización de botones y etiquetas del teclado para poder ejecutar
		los métodos que los configuran.*/	
		for (int o = 0; o < bNotas.length; o++){
			octava[o] = new JLabel();
			for (int i = 0; i < bNotas[o].length; i++){
				bNotas[o][i] = new JButton();
			}
		}
						
		v.add(construirPanel());
		v.setVisible(true);

	}
	
	//Construcción del panel principal.
	JPanel construirPanel(){
	
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));		
		p.add(Box.createRigidArea(new Dimension (0, 10)));
		p.add(construirpTeclado());
		p.add(Box.createRigidArea(new Dimension (0, 10)));
		p.add(construirpResultados());
		p.add(Box.createRigidArea(new Dimension (0, 10)));
		
		return p;

	}
	
	//Construcción del panel que aloja el teclado.
	JPanel construirpTeclado(){
	
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(Box.createRigidArea(new Dimension(10, 0)));
		
		JLayeredPane lp = new JLayeredPane();
		lp.setOpaque(true);
        lp.setBorder(BorderFactory.createTitledBorder(idioma[0]));
        
		Font numeroOctava = new Font("sansserif", Font.PLAIN, 11);
		for (int o = 0; o < octava.length; o++){
			octava[o].setText(String.valueOf(o));
			octava[o].setLocation((98 * o) + 18, 20);
			octava[o].setOpaque(true);
			octava[o].setSize(20, 20);
			octava[o].setVisible(true);
			octava[o].setFont(numeroOctava);
			lp.add(octava[o], new Integer(1));
		}
		
		construirTeclado();
		 		
   		for (int o = 0; o < bNotas.length; o++){
        	for (int i = 0; i < bNotas[o].length; i++){
        
        		if (esTeclaNegra(i) == true){
        			lp.add(bNotas[o][i], new Integer(2));
        		} else {
        			lp.add(bNotas[o][i], new Integer(1));
       		 	}
		
			}        
        }
        
        soDif.setSize(6, 45);
        soDif.setBackground(verde);
        soDif.setVisible(false);
        soDif.setLocation(90, 85);
        soDif.setOpaque(true);
        lp.add(soDif, new Integer(3));
        
		p.add(lp);
		p.add(Box.createRigidArea(new Dimension(10, 0)));
	
		return p;
		
	}
	
	//Construcción del panel que mostrará los segundaNotas.
	JPanel construirpResultados(){
	
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(Box.createRigidArea(new Dimension(10, 0)));	
		p.add(panelRy());
		p.add(Box.createRigidArea(new Dimension(10, 0)));
	
		return p;
		
	}
	
	//Construcción de los paneles que integran el panel segundaNotas.
	JPanel panelRy(){
	
		JPanel p = new JPanel();
		Dimension d = new Dimension(0, 15);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createTitledBorder(idioma[1]));
		p.add(Box.createRigidArea(d));
		p.add(panelRx());
		p.add(Box.createRigidArea(d));
		p.add(panelCI());
		p.add(Box.createRigidArea(d));
			
		return p;
	
	}
	
	JPanel panelRx(){
	
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setMaximumSize(new Dimension(830, 300));
		
		Dimension d = new Dimension(20, 0);
		
		p.add(panelPrimeraNota());
		p.add(Box.createRigidArea(d));
		p.add(panelSegundaNota());
		p.add(Box.createRigidArea(d));
		p.add(panelDiferencial());
	
		return p;
	
	}
	
	JPanel panelPrimeraNota(){
	
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setMaximumSize(new Dimension(260, 300));
	
			JPanel pp = new JPanel();
			pp.setOpaque(true);
			pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
		
			JLabel nota1 = new JLabel(idioma[3], JLabel.CENTER);
			nota1.setVisible(true);
			nota1.setSize(60, 30);
		
			primeraNota.setVisible(true);
			primeraNota.setEditable(false);
			primeraNota.setHorizontalAlignment(JTextField.CENTER);
			primeraNota.setBackground(blanco);
			primeraNota.setFont(dataObtenidaFont);
			
			pp.add(nota1);
			pp.add(Box.createRigidArea(new Dimension(3, 0)));
			pp.add(primeraNota);
		
			JPanel ppp = new JPanel();
			ppp.setOpaque(true);
			ppp.setLayout(new BoxLayout(ppp, BoxLayout.X_AXIS));
			ppp.setBorder(BorderFactory.createTitledBorder(idioma[2]));
			ppp.setPreferredSize(new Dimension(240, 80));
		
			slider[0] = new JSlider(JSlider.HORIZONTAL, 1, 16, 1);
			slider[0].setVisible(true);
			slider[0].setMajorTickSpacing(3);
			slider[0].setMinorTickSpacing(1);
			slider[0].setPaintTicks(true);
			slider[0].setPaintLabels(true);
	
			ppp.add(slider[0]);
			
		p.add(ppp);
		p.add(Box.createRigidArea(new Dimension(0, 3)));
		p.add(pp);
		
		return p;
	
	}
	
	JPanel panelSegundaNota(){
	
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setMaximumSize(new Dimension(260, 300));
	
			JPanel pp = new JPanel();
			pp.setOpaque(true);
			pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
		
			JLabel nota2 = new JLabel(idioma[4], JLabel.CENTER);
			nota2.setVisible(true);
			nota2.setSize(60, 30);
		
			segundaNota.setVisible(true);
			segundaNota.setEditable(false);
			segundaNota.setHorizontalAlignment(JTextField.CENTER);
			segundaNota.setBackground(blanco);
			segundaNota.setFont(dataObtenidaFont);
			
			pp.add(nota2);
			pp.add(Box.createRigidArea(new Dimension(3, 0)));
			pp.add(segundaNota);
		
			JPanel ppp = new JPanel();
			ppp.setOpaque(true);
			ppp.setLayout(new BoxLayout(ppp, BoxLayout.X_AXIS));
			ppp.setBorder(BorderFactory.createTitledBorder(idioma[2]));
			ppp.setPreferredSize(new Dimension(240, 80));
		
			slider[1] = new JSlider(JSlider.HORIZONTAL, 1, 16, 1);
			slider[1].setVisible(true);
			slider[1].setMajorTickSpacing(3);
			slider[1].setMinorTickSpacing(1);
			slider[1].setPaintTicks(true);
			slider[1].setPaintLabels(true);
	
			ppp.add(slider[1]);
			
		p.add(ppp);
		p.add(Box.createRigidArea(new Dimension(0, 3)));
		p.add(pp);
		
		return p;
	
	}
	
	JPanel panelDiferencial(){
	
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setMaximumSize(new Dimension(260, 300));
	
			JPanel pp = new JPanel();
			pp.setOpaque(true);
			pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
			pp.setMaximumSize(new Dimension(270, 25));
			
			JLabel diferenciallabel = new JLabel(idioma[5], JLabel.CENTER);
			diferenciallabel.setVisible(true);
		
			diferencial.setVisible(true);
			diferencial.setEditable(false);
			diferencial.setBackground(blanco);
			diferencial.setHorizontalAlignment(JTextField.CENTER);
			diferencial.setFont(dataObtenidaFont);
			
			pp.add(diferenciallabel);
			pp.add(Box.createRigidArea(new Dimension(3, 0)));
			pp.add(diferencial);
		
			JPanel ppp = new JPanel();
			ppp.setOpaque(true);
			ppp.setLayout(new BoxLayout(ppp, BoxLayout.X_AXIS));
		
			verMemorias.setText(idioma[6]);
			verMemorias.setVisible(true);
			verMemorias.addActionListener(this);

			borrar.setText(idioma[7]);
			borrar.setVisible(true);
			borrar.addActionListener(this);
			
			ppp.add(verMemorias);
			ppp.add(Box.createRigidArea(new Dimension(3, 0)));
			ppp.add(borrar);
			
		
		p.add(Box.createRigidArea(new Dimension(0, 35)));
		p.add(pp);
		p.add(Box.createRigidArea(new Dimension(0, 5)));
		p.add(ppp);
		
		return p;
	
	}
	
	JPanel panelCI(){
	
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		
		JLabel classInfo = new JLabel("<html><div align='center'>http://sourceforge.net/projects/calsd<br>Rodrigo Valla</div><html>", JLabel.CENTER);
	
		p.add(classInfo);
	
		return p;
	
	}
	
	void getIdioma(String s){
	
		if (s.equals("es")){
			idioma[0] = "Teclado";
			idioma[1] = "Análisis";
			idioma[2] = "Armónico";
			idioma[3] = "Primera nota:";
			idioma[4] = "Segunda nota:";
			idioma[5] = "Diferencial:";
			idioma[6] = "Historial";
			idioma[7] = "Borrar";
		} else {
			idioma[0] = "Keyboard";
			idioma[1] = "Analysis";
			idioma[2] = "Harmonic";
			idioma[3] = "First note:";
			idioma[4] = "Second note:";
			idioma[5] = "Differential tone:";
			idioma[6] = "History";
			idioma[7] = "Erase";
		}	
		
	}
	
	/*/////////////////////////////////////////////////
	Métodos para la construcción y gestión del teclado.
	/////////////////////////////////////////////////*/
	void construirTeclado(){
	
		for (int o = 0; o < bNotas.length; o ++){
			for (int i = 0; i < bNotas[o].length; i++){
			
       			bNotas[o][i].setOpaque(true);
   				bNotas[o][i].setBorder(null);
				bNotas[o][i].setBorderPainted(false);
				bNotas[o][i].addActionListener(this);
			
				//Diferenciación del tamaño de las teclas.
				if (esTeclaNegra(i) == true){			
					bNotas[o][i].setSize(6, 35);
				} else {
					bNotas[o][i].setSize(12, 60);
				}
			
	        	pintarTeclado();
				posicionarTeclado();
	        	bNotas[o][i].setVisible(true);
	        	
        	}	
		} 
		
	}
	
	//Método para pintar el teclado a blanco y negro.
	void pintarTeclado(){
	
		for (int o = 0; o < bNotas.length; o ++){
			for (int i = 0; i < bNotas[o].length; i++){
			
				if (esTeclaNegra(i) == true){			
					bNotas[o][i].setBackground(negro);
				} else {
					bNotas[o][i].setBackground(blanco);
				}
			
			}
		}
	
	}
	
	/* Método que posiciona las teclas (pnicial corresponde al margen con respecto al
	borde del panel, ajuste permite corregir el error que se produce cuando aparecen
	dos teclas blancas consecutivas, ajusteNegras permite centrar las teclas negras con
	respecto a las blancas y ajusteOctava ubica las octavas en forma sucesiva.*/
	void posicionarTeclado(){
	
		int pinicial = 16;
		int ajuste = 0;
		int ajusteNegras = 3;
		int ajusteOctava = 98;
		
		for (int o = 0; o < bNotas.length; o++){
			
			ajuste = 0;
			
			for (int i = 0; i < bNotas[o].length; i++){
			
				if (i >= 5){
					ajuste = 7;
				}
			
				if (esTeclaNegra(i) == true){			
					bNotas[o][i].setLocation(pinicial + (7 * i) + ajuste + (ajusteOctava * o) + ajusteNegras, 40);
				} else {
					bNotas[o][i].setLocation(pinicial + (7 * i) + ajuste + (ajusteOctava * o), 40);
				}
			
			}
		}
	
	}
	
	//Métodos para activar y desactivar el teclado.
	void activarTeclado(){
		for (int o = 0; o < bNotas.length; o++){
			for (int i=0; i < bNotas[o].length; i++){
				bNotas[o][i].setEnabled(true);
			}
		}
	}

	void desactivarTeclado(){
		for (int o = 0; o < bNotas.length; o++){
			for (int i=0; i < bNotas[o].length; i++){
				bNotas[o][i].setEnabled(false);
			}
		}
	}

	//Método para decidir si el botón corresponde a una tecla blanca o negra.
	boolean esTeclaNegra(int i){
	
		boolean esTN = false;
		if (i == 1 || i == 3 || i == 6 || i == 8 || i == 10){
			esTN = true;
		}
		return esTN;
	
	}
	
	/*//////////////////////////////////////////
	Métodos para el funcionamiento del programa
	//////////////////////////////////////////*/
	public void actionPerformed (ActionEvent ae) {
	
		for (int o = 0; o < bNotas.length; o++){
			for (int i=0; i < bNotas[o].length; i++ ){
			
				if(ae.getSource() == bNotas[o][i]){
			
					frecuenciaNota = doCero * Math.pow(Math.pow(2, 1.0/12.0), i) * Math.pow(2, o);
					
					if (esPrimeraNota == true){
						pintarTeclado();
						soDif.setVisible(false);
						f1 = getFrecuenciaAr(frecuenciaNota, 0);
						primeraNota.setText(z.format(f1) + " Hz");
						esPrimeraNota = false;
						segundaNota.setText("");
						diferencial.setText("");
						if(mostrandoMemoria == false){
							historial[posicionMemoria][0] = o;
							historial[posicionMemoria][1] = i;
							historial[posicionMemoria][2] = slider[0].getValue();
						}
       				} else {
						f2 = getFrecuenciaAr(frecuenciaNota, 1);
						segundaNota.setText(z.format(f2) + " Hz");
						esPrimeraNota = true;
						getDiferencial(f1, f2);
						if(mostrandoMemoria == false){
							historial[posicionMemoria][3] = o;
							historial[posicionMemoria][4] = i;
							historial[posicionMemoria][5] = slider[0].getValue();
							historial[posicionMemoria][5] = slider[1].getValue();
							posicionMemoriaVer = contadorEjecutar;
							gestionarPosicionMemoria();
						}
					}
					
					if (esTeclaNegra(i) == true){			
						bNotas[o][i].setBackground(azul);
					} else {
						bNotas[o][i].setBackground(rojo);
					}
				
					if (hayInstrumentos == true){
						playN((12 * o) + 12 + i);
					}
				
				
					
				}
						
			}
		}
		
		if(ae.getSource() == borrar) {
			
			primeraNota.setText("");
			segundaNota.setText("");
			diferencial.setText("");
			pintarTeclado();
			activarTeclado();
			soDif.setVisible(false);
			esPrimeraNota = true;
			mostrandoMemoria = false;
			
		}
		
		if(ae.getSource() == verMemorias){
			
			pintarTeclado();
			
			if (0 < contadorEjecutar && contadorEjecutar < 12) {
				
				if(0 < posicionMemoriaVer){
					mostrarMemoria(posicionMemoriaVer);
					posicionMemoriaVer = posicionMemoriaVer - 1;
				} else {
					mostrarMemoria(posicionMemoriaVer);
					posicionMemoriaVer = contadorEjecutar - 1;
				}

			} else if (11 < contadorEjecutar){

				if (posicionMemoriaVer < 12){
					posicionMemoriaVer = posicionMemoriaVer + 12;
				}
			
				mostrarMemoria(posicionMemoriaVer - 12);
				posicionMemoriaVer = posicionMemoriaVer - 1;

			} else {
			
			}

		}
	
	}
	
	
	
	/*///////////////////////////////////
	Métodos para al producción de sonido.
	///////////////////////////////////*/
	
	//Método para hacer sonar las teclas.
	void playN(int n){
	
		canal.noteOn(n, 100);
       	try {
       		Thread.sleep(10);
           	} catch (InterruptedException e) {
          
           	} finally {
           	canal.noteOff(n);
           	}
        
    }
    
    /*////////////////////////////////
	Gestión del historial de análisis
	////////////////////////////////*/
	void mostrarMemoria(int x){
			
		if (mostrandoMemoria == false){
			mostrandoMemoria = true;
			desactivarTeclado();
		}
		
		bNotas[historial[x][0]][historial[x][1]].setEnabled(true);
		slider[0].setValue(historial[x][2]);
		bNotas[historial[x][0]][historial[x][1]].doClick();
		bNotas[historial[x][0]][historial[x][1]].setEnabled(false);
		
		try {
       		Thread.sleep(40);
           	} catch (InterruptedException e) {
          
           	} finally {
           	bNotas[historial[x][3]][historial[x][4]].setEnabled(true);
			slider[1].setValue(historial[x][5]);
			bNotas[historial[x][3]][historial[x][4]].doClick();
			bNotas[historial[x][3]][historial[x][4]].setEnabled(false);
        }
		
	}

	void gestionarPosicionMemoria(){
		
		if (contadorEjecutar == 24){
			contadorEjecutar = contadorEjecutar - 11;
		} else {
			contadorEjecutar = contadorEjecutar + 1;
		}
		
		if (posicionMemoria == 11){
				posicionMemoria = 0;
			} else {
				posicionMemoria = posicionMemoria + 1;
		}
			
	}
	
	/*///////////////////
	Gestión del análisis
	///////////////////*/
	
	/*Método que recibe dos frecuencias y muestra su diferencia en el cuadro de texto
	correspondiente ubicando también el indicador de afinación.*/
	void getDiferencial(double d, double e){
	
		double dif = 0;
		if (d > e){
			dif = d - e;
		} else {
			dif = e - d;
		}
		
		if (dif > 20 && dif < 20000){
			diferencial.setText(z.format(dif) + " Hz");
			if (dif < 8000){
				posicionarDiferencial(dif);
			}
		} else {
			diferencial.setText("Fuera de rango");
		}
		
	}
	
	//Método que calcula la frecuencia del armónico pedido por el usuario.
	double getFrecuenciaAr(double x, int n){
	
		int c = slider[n].getValue();
		while (x * c >= 20000){
			c = c - 1;
		}
		slider[n].setValue(c);
		double f = x * c;
		
		return f;
	
	}
	
	
	//Método para posicionar el indicador de la frecuencia.
	void posicionarDiferencial(double f){
	
		soDif.setVisible(false);
		double n = logT(f);
		soDif.setLocation(coordenadaX(n), soDif.getY());
		soDif.setVisible(true);
			
	}
	
	/* Método que devuelve un número que indica el corrimiento hacia la derecha para
	el diferencial, con respecto a la primera nota del teclado.*/
	double logT (double f){
		
		double t = Math.pow(2, 1.0/12.0);
		double n = (Math.log(f/doCero)/Math.log(t));
		
		return n;
		
	}

	/*Método que convierte el corrimiento hacia la derecha para una frecuencia y
	devuelve la coordenada horizontal para el indicador.*/
	int coordenadaX(double x){
	
		int coordenada = 0;
		double c = 0.0;
		int pinicial = 19;
		int nota = (int) Math.round(x%12);
		if (nota == 12){
			nota = nota - 12;
		} 
		
		//8.17 es una constante relacionada con el ancho de la octava.
		c = (x*8.17);
		
		int correccion = 0;
		if (nota >= 3 && nota < 5){
			correccion = -1;
		} else if (nota >= 5 && nota <= 8){
			correccion = 6;
		} else if (nota > 7){
			correccion = 5;
		}
		
		coordenada = ((int) Math.round(c)) + pinicial - nota + correccion;
		
		return coordenada;
		
	}
	
}