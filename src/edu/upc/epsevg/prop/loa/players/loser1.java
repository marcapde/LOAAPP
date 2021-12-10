package edu.upc.epsevg.prop.loa.players;

import edu.upc.epsevg.prop.loa.CellType;
import edu.upc.epsevg.prop.loa.GameStatus;
import edu.upc.epsevg.prop.loa.IAuto;
import edu.upc.epsevg.prop.loa.IPlayer;
import edu.upc.epsevg.prop.loa.Move;
import edu.upc.epsevg.prop.loa.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
/**
 * Jugador humà de LOA
 * @author bernat
 */
public class loser1 implements IPlayer,IAuto {

    String name;
    CellType color;
    private GameStatus scopy;
    private int numNodes;
    private int maxDepth;
    private boolean solFound;
    private boolean timeout;
    private int numJugades;
    public class tagHASH{
        int id;
        int heur;
        int prof;//quants nivells hem avaluat per sota
        Move best;
    };
    HashMap <Integer,tagHASH> mapa;
    private long[][][] Zobrist ;
    public int[][] tablaPuntuacio = {
        {3, 4, 5, 7, 7, 5, 4, 3},
        {4, 6, 8,10,10, 8, 6, 4},
        {5, 8,11,13,13,11, 8, 5},
        {7,10,13,16,16,13,10, 7},
        {7,10,13,16,16,13,10, 7},
        {5, 8,11,13,13,11, 8, 5},
        {4, 6, 8,10,10, 8, 6, 4},
        {3, 4, 5, 7, 7, 5, 4, 3}
    };
    
    public void initializeHASH(){
        Random r = new Random();
        for (int i=0;i<8;i++){
            for (int j=0;j<8;j++){
                for (int k=0;k<2;k++){
                    Zobrist [i][j][k]=r.nextInt();
                }                    
            }
        }
    }
    
    
    
    public loser1(String name) {
        this.name = name;
        numNodes = 0;
        maxDepth = 0;
        numJugades = 0;
        initializeHASH();
    }

    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que
     * ha de posar.
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus s) {
        mapa = new HashMap<Integer,tagHASH>();
        color = s.getCurrentPlayer();
        Point firstMove= s.getPiece(color,0);
        Point firstTo= s.getMoves(firstMove).remove(0);
        Move res = new Move(firstMove,firstTo,0,0,SearchType.MINIMAX);
        Move oldRes = res;
        int i = 2;
        solFound=false;
        timeout=false;
        while (!solFound && !timeout){
            res = minimax(s,i);
            if(!timeout) oldRes = res;
            else break;
            i+=1;
            maxDepth = i;//cuando incrementar maxDepth????????
        }
        System.out.println("Profunditat:"+maxDepth);
        System.out.println("Jugades:"+ ++numJugades);
        return oldRes;
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public void timeout() {
        // Bah! Humans do not enjoy timeouts, oh, poor beasts !
        System.out.println("Bah! You are so slow...");
        timeout = true;
    }

    /**
     * Retorna el nom del jugador que s'utlilitza per visualització a la UI
     *
     * @return Nom del jugador
     */
    @Override
    public String getName() {
        return "(" + name + ")";
    }
    
    
    
    
    
     public Move minimax(GameStatus s, int pprof){
        
        int valor = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        
        int numFitxes = s.getNumberOfPiecesPerColor(color);
        ArrayList<Point> fitxesPos = new ArrayList<>();
        Point bestFrom= new Point (-100,-100);
        Point bestTo=new Point (-100,-100);
        for (int i = 0; i < numFitxes; i++) {
            //desem on estan totes les fitxes
            fitxesPos.add(s.getPiece(color, i));
        }
       
        
        for (int i = 0; i < numFitxes; i++){
            Point fromAct = fitxesPos.remove(0);

            //suponemos que el remove actualiza las posiciones del array
            ArrayList<Point> movPosi = s.getMoves(fromAct);
            if(movPosi.size() > 0 ){
                
                for (int j=0;j<movPosi.size();j++){
                    scopy = new GameStatus(s);
                    Point movAct = movPosi.remove(0);
                    scopy.movePiece(fromAct, movAct);
                    int valorNou = movMin(scopy, movAct ,pprof-1, alpha, beta);
                    if (timeout) break;
                    if(valorNou > valor){
                        valor = valorNou;
                        bestFrom = fromAct;
                        bestTo = movAct;
                        
                    }
                } 
                if (timeout) break;
            } 
        }//if (bestFrom.x == -100) return new Move(bestFrom,bestTo,0,maxDepth,SearchType.MINIMAX);
        
        return new Move(bestFrom,bestTo,numNodes,maxDepth,SearchType.MINIMAX);
    }

    /**
     * Función que nos devuelve el movimiento con mayor valor heurístico de todos los
     * movimientos estudiados.
     * @param ps tablero resultante de poner una ficha en una determinada columna.
     * @param lastPoint columna en la que hemos puesto ficha para estudiar el tablero.
     * @param pprof número de niveles restantes que le queda al algoritmo por
     * analizar.
     * @param alpha variable que determina el alfa para realizar la poda alfa-beta.
     * @param beta variable que determina la beta para realizar la poda alfa-beta.
     * @return retorna el valor heurístico máximo entre todas las posibilidades
     * comprobadas.
     */
    public int movMax(GameStatus ps, Point lastPoint ,int pprof,int alpha,int beta){
        //System.out.println("HOLAMAX");
        if(ps.isGameOver() && ps.GetWinner() != color){ //Perdem
           return -100000;
        }else if(ps.isGameOver() && ps.GetWinner() == color){//Ganamos
            solFound = true;
            return 100000;
        }else if(timeout){
            return Integer.MIN_VALUE;
        }else if (pprof == 0 ){//peta
            return Heuristica(ps);
        }
        
        //maxDepth = maxDepth + 1;//Si arribem a aquest punt, hem augmentat la profunditat.
        
        int value = Integer.MIN_VALUE;
        
        int numFitxes = ps.getNumberOfPiecesPerColor(color);
        ArrayList<Point> fitxesPos = new ArrayList<>();
        for (int i = 0; i < numFitxes; i++) {
            //desem on estan totes les fitxes
            fitxesPos.add(ps.getPiece(color, i));
        }
        
       
        for(int i = 0; i < numFitxes; i++){
            Point fromAct = fitxesPos.remove(0);
            //suponemos que el remove actualiza las posiciones del array
            ArrayList<Point> movPosi = ps.getMoves(fromAct);
            if(movPosi.size() > 0 ){
                for (int j=0;j<movPosi.size();j++){
                    ////////////////////////////////////////
                    GameStatus scopy2 = new GameStatus(ps);//es pot fer sense new?
                    ////////////////////////////////////////
                    Point movAct = movPosi.remove(0);
                    //System.out.println("HOLAMAX2");
                    scopy2.movePiece(fromAct, movAct);//Movemos la pieza
                    
                    value = Math.max(value, movMin(scopy2, movAct , pprof -1,alpha,beta));//********Mirar parametros******
                    alpha = Math.max(value,alpha);
                    if(alpha>=beta)
                    {
                        break;
                    }                    
                   
                }   
              
            }
        }
       
        return value;
    }

    /**
     * Función que nos devuelve el movimiento con menor valor heurístico de todos los
     * movimientos estudiados.
     * @param ps tablero resultante de poner una ficha en una determinada columna.
     * @param lastPoint columna en la que hemos puesto ficha para estudiar el tablero.
     * @param pprof número de niveles restantes que le queda al algoritmo por
     * analizar.
     * @param alpha variable que determina el alfa para realizar la poda alfa-beta.
     * @param beta variable que determina la beta para realizar la poda alfa-beta.
     * @return retorna el valor heurístico máximo entre todas las posibilidades
     * comprobadas.
     */

    public int movMin(GameStatus ps,Point lastPoint, int pprof,int alpha, int beta){///Mirar parametros
        //System.out.println("HOLAMIN");
        if(ps.isGameOver() && ps.GetWinner() != color){ //Perdem
           return -100000;
        }else if(ps.isGameOver() && ps.GetWinner() == color){//Ganamos
            solFound = true;
            return 100000;
        }else if(timeout){
            return Integer.MIN_VALUE;
        }else if (pprof == 0){
            return Heuristica(ps);
        }
        
        
        //maxDepth = maxDepth + 1;//Si arribem a aquest punt, hem augmentat la profunditat.
        
        int value = Integer.MAX_VALUE;
        
        CellType colorRival = CellType.opposite(color);
        
        int numFitxes = ps.getNumberOfPiecesPerColor(colorRival);
        
        ArrayList<Point> fitxesPos = new ArrayList<>();
        for (int i = 0; i < numFitxes; i++) {
            //desem on estan totes les fitxes
            fitxesPos.add(ps.getPiece(colorRival, i));
        }
       
        
        for(int i = 0; i < numFitxes; i++){
            Point fromAct = fitxesPos.remove(0);
            //suponemos que el remove actualiza las posiciones del array
            ArrayList<Point> movPosi = ps.getMoves(fromAct);
            
            if(movPosi.size() > 0 ){
                for (int j=0;j<movPosi.size();j++){
                    GameStatus scopy2 = new GameStatus(ps);
                    Point movAct = movPosi.remove(0);
                    scopy2.movePiece(fromAct, movAct);//Movemos la pieza
                    
                    value = Math.min(value, movMax(scopy2, movAct ,pprof -1,alpha,beta));
                    
                    beta = Math.min(value,beta);
                    if(alpha>=beta)
                    {
                        break;
                    }                     
                }   
            }
        }
        
        return value;
    }
    
    public int Heuristica(GameStatus ps){
        int valorHeur = BlockHeur(ps);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (ps.getPos(i,j) == color) {
                    valorHeur += tablaPuntuacio[i][j];
                } else if (ps.getPos(i,j) == CellType.opposite(color )) {
                    valorHeur -= tablaPuntuacio[i][j];
                }
            }
        }
        return valorHeur;

    }
    public int BlockHeur(GameStatus ps){
        int res = DistHeur(ps);
        //conseguir array de caselles enemigues
        CellType colorRival = CellType.opposite(color);
        int numFitxes = ps.getNumberOfPiecesPerColor(colorRival);
        
        //ArrayList<Point> fitxesPos = new ArrayList<>();
        for (int i = 0; i < numFitxes; i++) {
            //desem on estan totes les fitxes
            Point pos = (ps.getPiece(colorRival, i));
            res -= 2 * ps.getMoves(pos).size();
        }        
        return res;
        
    }
    public int DistHeur (GameStatus ps){
        int res = 0;
        int numFitxes = ps.getNumberOfPiecesPerColor(color);
        
        //ArrayList<Point> fitxesPos = new ArrayList<>();
        for (int i = 0; i < numFitxes; i++) {
            Point act = ps.getPiece(color, i);
            for (int j=i+1;j<numFitxes;j++){
                Point next = (ps.getPiece(color, j));
                res -= getDistance(act,next);
                //res -= 2 * ps.getMoves(pos).size();
            }
            
        }        
        return res/numFitxes;
    }
    private int getDistance(Point act, Point next){
        int dist = 0;
        //obtenir distancia per caselles
//        int xAct = act.x;
//        int yAct = act.y;
//        int xNext = next.x;
//        int yNext = next.y;
        dist = Math.abs(act.x - next.x + (act.y-next.y)-1); 
        return dist;        
    }
    //algorithm steps:
    //1.Al inici de la partida, generar una array de 12*64 
    //
    //
    //
    //
}


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
  