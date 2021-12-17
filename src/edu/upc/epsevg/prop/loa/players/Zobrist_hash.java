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
public class Zobrist_hash implements IPlayer,IAuto {

    /**
     * Variables
     */
    String name;
    CellType color;
    private GameStatus scopy;
    private int numNodes;
    private int maxDepth;
    private boolean solFound;
    private boolean timeout;
    private int numJugades;
    private int heurAux;
    private SearchType cerca;

    
    
    public class tagHASH{
        public long id;
        public int heur;
        public int prof;//quants nivells hem avaluat per sota
        public Move best;

        private tagHASH(long pid, int pheur, int pprof, Move pbest) {
            id = pid;
            heur = pheur;
            prof = pprof;
            best = pbest;
        }
     
    };
    
    HashMap <Long,tagHASH> mapa;
    
    private long[][][] Zobrist = new long [8][8][2];
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
    
    
    /**
     * Funciones
     */
    
    
    
    
    
    public void initializeHASH(){
        Random r = new Random();
        for (int i=0;i<8;i++){
            for (int j=0;j<8;j++){
                for (int k=0;k<2;k++){
                    Zobrist [i][j][k]=r.nextLong();
                    //(k=0 == white)(k=1 == black)
                                 
//                    if((k==0) && (i!=0 && i!=7) && (j==0 || j == 7)){
//                        Zobrist[i][j][k]=(Zobrist[i][j][k]^k);
//                    }
//h ^ zobrist
//
//                    if((k==1) && (j!=0 && j!=7) && (i==0 || i == 7)){
//                         Zobrist[i][j][k]=(Zobrist[i][j][k]^k);
//                    }
//                    
                }                    
            }
        }
    }
    
    
    public Zobrist_hash(String name,SearchType pcerda) {
        this.name = name;
        numNodes = 0;
        maxDepth = 0;
        numJugades = 0;
        initializeHASH();
        cerca = pcerda;
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
       color = s.getCurrentPlayer();
       if (cerca == SearchType.MINIMAX_IDS){
           return minmaxIDS(s);
       }
       return minimax(s,8);// profinfitat default = 8
    }
    
    public Move minmaxIDS(GameStatus s){
        mapa = new HashMap<Long,tagHASH>();
        Point firstMove= s.getPiece(color,0);
        Point firstTo= s.getMoves(firstMove).remove(0);
        Move res = new Move(firstMove,firstTo,0,0,cerca);
        Move oldRes = res;
        int oldHeur=0;
        int i = 2;
        solFound=false;
        timeout=false;
        
        while (!solFound && !timeout){
            res = minimax(s,i);
            if(!timeout){
               oldRes = res;
               oldHeur = heurAux;
            }
            else break;
            i+=1;
            maxDepth = i;//cuando incrementar maxDepth????????
        }
        System.out.println("Profunditat:"+maxDepth);
        System.out.println("Jugades:"+ ++numJugades);
        
        //Codi zobritz hashing
            Point fr=oldRes.getFrom();
            Point to=oldRes.getTo();
            int k= CellType.toColor01(color);
 

            
        //Fi codi zobritz hashing
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
        Point fromAct = null;
        Point movAct = null;
        int h = GetHashTauler(s);
        //ens assegurem de que si el mapa conte el tauler, que sigui el correcte
        if (mapa.containsKey(h) && mapa.get(h) != null  
                && fitxesPos.contains(mapa.get(h).best.getFrom())
                && s.getMoves(mapa.get(h).best.getFrom()).contains(mapa.get(h).best.getTo())){
            fromAct = mapa.get(h).best.getFrom();
            fitxesPos.remove(fromAct);
            movAct = mapa.get(h).best.getTo();
        }
        
        int valorNou = Integer.MIN_VALUE;
        for (int i = 0; fitxesPos.size()>0; i++){
            //comencem pel millor, en cas de que existeixi
            if (i >= 1 || fromAct == null){
                fromAct = fitxesPos.remove(0);
            }                   
            //suponemos que el remove actualiza las posiciones del array
            //ArrayList<Point> movPosi = s.getMoves(fromAct);
            ArrayList<Point> movPosi = s.getMoves(fromAct);
            if(movPosi.size() > 0 ){                
                for (int j=0;movPosi.size()>0;j++){
                    scopy = new GameStatus(s);
                    if (j == 0 && i == 0 && movAct != null){
                        //movAct = mapa.get(h).best.getTo();
                        //tagHASH th = mapa.get(h);
                        //alpha = th.heur;
                        //bestFrom = th.best.getFrom();
                        //bestTo = th.best.getTo();
                        movPosi.remove(movAct);
                    }else{
                        movAct = movPosi.remove(0);
                    }                    
                    scopy.movePiece(fromAct, movAct);
                    //actualitza hash
                    long hash = actualizaHash(h,fromAct,movAct,s);
                    
                    
                    //int xx= GetHashTauler(scopy);
//                    if(mapa.containsKey(xx) && mapa.get(xx)!=null){
//                        tagHASH th =  mapa.get(xx);
//                            alpha=th.heur;
//                        
//                        
//                            bestFrom = th.best.getFrom();
//                            bestTo = th.best.getTo();
//                            heurAux = valorNou;
//                            
//                        if (timeout) break;
//                    }
                    valorNou = movMin(scopy, movAct ,pprof-1, alpha, beta,hash);
                    if (timeout) break;
                    if(valorNou > alpha){
                        alpha = valorNou;
                        bestFrom = fromAct;
                        bestTo = movAct;
                        heurAux = valorNou;
                    }
                    
                } 
                if (timeout) break;
            } 
        }
        Move movement = new Move(bestFrom,bestTo,numNodes,maxDepth,SearchType.MINIMAX);
        //Zobrist
        
        tagHASH th = new tagHASH(h,heurAux,maxDepth,movement);
        AddToHASH(s,th,h);            

        
        //Fi ZObrist
        return movement;
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
    public int movMax(GameStatus ps, Point lastPoint ,int pprof,int alpha,int beta, long h){
        //System.out.println("HOLAMAX");
        //th.best(Move());
        if(ps.isGameOver() && ps.GetWinner() != color){ //Perdem
//            int x = AddToHASH(ps,th);
            return -100000;
        }else if(ps.isGameOver() && ps.GetWinner() == color){//Ganamos
            solFound = true;
            //afegir tauler a hash
            return 100000;
        }else if(timeout){
            return Integer.MIN_VALUE;
        }else if (pprof == 0 ){//peta
            //afegir tauler a hash
            return Heuristica(ps);
        }
        //System.out.println("HolaMax");
        //maxDepth = maxDepth + 1;//Si arribem a aquest punt, hem augmentat la profunditat.
        
        int value = Integer.MIN_VALUE;
        
        int numFitxes = ps.getNumberOfPiecesPerColor(color);
        ArrayList<Point> fitxesPos = new ArrayList<>();
        for (int i = 0; i < numFitxes; i++) {
            //desem on estan totes les fitxes
            fitxesPos.add(ps.getPiece(color, i));
        }
        
        Move mv=new Move(lastPoint,lastPoint,0,0,SearchType.MINIMAX_IDS);
        Point fromAct = null;
        Point movAct = null;
        //long h = hash;
        //ens assegurem de que si el mapa conte el tauler, que sigui el correcte
        if (mapa.containsKey(h) && mapa.get(h) != null  
                && fitxesPos.contains(mapa.get(h).best.getFrom())
                && ps.getMoves(mapa.get(h).best.getFrom()).contains(mapa.get(h).best.getTo())){
            fromAct = mapa.get(h).best.getFrom();
            fitxesPos.remove(fromAct);
            movAct = mapa.get(h).best.getTo();
        }
        for(int i = 0; i < numFitxes; i++){
            //la h no caldria que la calcules a cada iteració, es podria fer eficient
            //comencem pel millor, en cas de que existeixi
            if (i >= 1 || fromAct == null){
                fromAct = fitxesPos.remove(0);
            }                   
            //suponemos que el remove actualiza las posiciones del array
            //ArrayList<Point> movPosi = s.getMoves(fromAct);
            ArrayList<Point> movPosi = ps.getMoves(fromAct); 
            if(movPosi.size() > 0 ){
                for (int j=0;movPosi.size()>0;j++){
                    //scopy = new GameStatus(s);
                    if (j == 0 && i == 0 && movAct != null){
                        //movAct = mapa.get(h).best.getTo();
                        //tagHASH th = mapa.get(h);
                        //alpha = th.heur;
                        //bestFrom = th.best.getFrom();
                        //bestTo = th.best.getTo();
                        movPosi.remove(movAct);
                    }else{
                        movAct = movPosi.remove(0);
                    }
                    ////////////////////////////////////////
                    GameStatus scopy2 = new GameStatus(ps);//es pot fer sense new? No, no es pot
                    ////////////////////////////////////////
//                        int x = GetHashTauler(scopy2);
//                        if(j == 0 && mapa.containsKey(x) && mapa.get(x)!=null ){
//                            tagHASH th = mapa.get(x);
//                            if(movPosi.contains(th.best.getTo())){
//                                alpha=th.heur;
//                                mv = th.best;
//                                movPosi.remove(th.best.getTo());
//                            }
                            //scopy2.movePiece(th.best.getFrom(),th.best.getTo()); 

                            //value = Math.max(value, movMin(scopy2,th.best.getTo(), pprof -1,alpha,beta));
                        //}
                        //else{
                            //movAct = movPosi.remove(0);
                            scopy2.movePiece(fromAct, movAct);
                            long haux = actualizaHash(h, fromAct, movAct, ps);
                            value = Math.max(value, movMin(scopy2, movAct , pprof -1,alpha,beta,haux));
                            if(value>alpha){
                                mv = new Move(fromAct,movAct,numNodes,maxDepth,SearchType.MINIMAX_IDS);
                                alpha = value;
                            }
                        //}
                        //System.out.println("HOLAMAX2");
                        //Movemos la pieza
                        //actualitzar hash

                       /********Mirar parametros******/
                        //alpha = Math.max(value,alpha);
                        if(alpha>=beta)
                        {
                            break;
                        } 
                }   
              
            }
        }
        tagHASH th = new tagHASH(h,alpha,maxDepth,mv);
        int a = AddToHASH(ps,th,h);
        return alpha;
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

    public int movMin(GameStatus ps,Point lastPoint, int pprof,int alpha, int beta,long hash){///Mirar parametros
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
        //System.out.println("HolaMin");
        
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
                    
                    
                    //Zobrist
//                    int x = GetHashTauler(scopy2);
//                        if(mapa.containsKey(x) && mapa.get(x)!=null){
//                            tagHASH th = mapa.get(x);
//                            if(movPosi.contains(th.best.getTo())){
//                               
//                               movPosi.remove(th.best.getTo());
//                            }
//                            value = Math.min(value, movMax(scopy2, th.best.getTo() ,pprof -1,alpha,beta));
//                        }else{
                            Point movAct = movPosi.remove(0);
                            scopy2.movePiece(fromAct, movAct);//Movemos la pieza
                            long haux = actualizaHash(hash, fromAct, movAct, ps);
                            value = Math.min(value, movMax(scopy2, movAct ,pprof-1,alpha,beta,haux));
//                        }
                         //Fi codi Zobrist
                    beta = Math.min(value,beta);
                    if(alpha>=beta)
                    {
                        break;
                    }                     
                }   
            }
        }
        
        return beta;
    }
        //algorithm steps:
    //1.Al inici de la partida, generar una array de 12*64 
    private int AddToHASH(GameStatus ps,tagHASH th,long hash){
        int added = 0;
        long hTauler = hash;
        tagHASH other = mapa.get(hTauler);
        if (other == null){
            added = 1;
            mapa.put(hTauler,th);
        }else if(th.prof > other.prof || 
                (th.prof == other.prof && th.heur > other.heur)){
            added = 1;
            mapa.put(hTauler,th);            
        } 
        return added;
    }
    
    private int GetHashTauler(GameStatus ps){
        int h = 0;
        int numFitxes = ps.getNumberOfPiecesPerColor(color);        
        for (int i = 0; i < numFitxes; i++) {
            Point pos = ps.getPiece(color, i);
            h^=Zobrist[pos.x][pos.y][CellType.toColor01(color)];
        }
        CellType oppo = CellType.opposite(color);
        numFitxes = ps.getNumberOfPiecesPerColor(oppo);
        for (int i = 0; i < numFitxes; i++) {
            Point pos = ps.getPiece(oppo, i);
            h^=Zobrist[pos.x][pos.y][CellType.toColor01(oppo)];
        }
        return h;
    }
    private long actualizaHash(long h, Point fromAct, Point movAct, GameStatus ps) {
        long hash = h;
        if (ps.getPos(movAct) == CellType.opposite(color)){
            //cas de que mengem al rival
            hash ^= Zobrist[movAct.x][movAct.y][CellType.toColor01(CellType.opposite(color))];
        }
        //treiem el from
        hash ^= Zobrist[fromAct.x][fromAct.y][CellType.toColor01(color)];
        //afegim el to
        hash ^= Zobrist[movAct.x][movAct.y][CellType.toColor01(color)];
        return hash;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public int Heuristica(GameStatus ps){
        int valorHeur = BlockHeur(ps) + DistHeur(ps);
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
        int res = 0;
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
        dist = Math.abs(act.x - next.x + (act.y-next.y)-1); 
        return dist;        
    }
    
    

}


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
  