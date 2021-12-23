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
public class Zobrist_min_alpha implements IPlayer,IAuto {

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
        //valors per taghashMax
        public boolean isMax;
        public int heurMax;
        public int profMax;//quants nivells hem avaluat per sota
        public Move bestMax;
        //valors per taghashMin
        public boolean isMin;
        public int heurMin;
        public int profMin;
        public Move bestMin;

        private tagHASH(long pid,boolean max, int pheurM, int pprofM, Move pbestM,
                boolean min, int pheurm, int pprofm, Move pbestm) {
            id = pid;
            isMax = max;
            heurMax = pheurM;
            profMax = pprofM;
            bestMax = pbestM;
            //-------------
            isMin = min;
            heurMin = pheurm;
            profMin = pprofm;
            bestMin = pbestm;
            
        }
     
    };
    
    HashMap <Long,tagHASH> mapa;
    
    private long[][][] Zobrist = new long [8][8][2];
    public int[][] tablaPuntuacio = {
        {-5, 4, 5, 7, 7, 5, 4, -5},
        {4, 6, 8,10,10, 8, 6, 4},
        {5, 8,11,13,13,11, 8, 5},
        {7,10,13,16,16,13,10, 7},
        {7,10,13,16,16,13,10, 7},
        {5, 8,11,13,13,11, 8, 5},
        {4, 6, 8,10,10, 8, 6, 4},
        {-5, 4, 5, 7, 7, 5, 4, -5}
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
    
    
    public Zobrist_min_alpha(String name,SearchType pcerda) {
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
       numNodes = 0;
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
        long h = GetHashTauler(s);
        //ens assegurem de que si el mapa conte el tauler, que sigui el correcte
        if (mapa.containsKey(h) && mapa.get(h) != null && mapa.get(h).isMax
                && fitxesPos.contains(mapa.get(h).bestMax.getFrom())
                && s.getMoves(mapa.get(h).bestMax.getFrom()).contains(mapa.get(h).bestMax.getTo())){
            bestFrom = fromAct = mapa.get(h).bestMax.getFrom();
            fitxesPos.remove(fromAct);
            bestTo = movAct = mapa.get(h).bestMax.getTo();
            alpha= mapa.get(h).heurMax;
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
                        //System.out.println("siuuu");
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
        //com el valor de min esta a false el ad ho ignorara així que posem qualsevol valor
        tagHASH th = new tagHASH(h,true,alpha,maxDepth,movement,false,alpha,maxDepth,movement);
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
        System.out.println(alpha);
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
        if (mapa.containsKey(h) && mapa.get(h) != null && mapa.get(h).isMax
                && fitxesPos.contains(mapa.get(h).bestMax.getFrom())
                && ps.getMoves(mapa.get(h).bestMax.getFrom()).contains(mapa.get(h).bestMax.getTo())){
            fromAct = mapa.get(h).bestMax.getFrom();
            fitxesPos.remove(fromAct);
            movAct = mapa.get(h).bestMax.getTo();
            if(beta>mapa.get(h).heurMax)beta= mapa.get(h).heurMax;
            mv = new Move(fromAct,movAct,numNodes,maxDepth,SearchType.MINIMAX_IDS);
        }
        for(int i = 0; i < numFitxes; i++){
            //la h no caldria que la calcules a cada iteració, es podria fer eficient
            //comencem pel millor, en cas de que existeixi
            if (i >= 1 || fromAct == null){
                fromAct = fitxesPos.remove(0);
            }                   
                        ArrayList<Point> movPosi = ps.getMoves(fromAct); 
            if(movPosi.size() > 0 ){
                for (int j=0;movPosi.size()>0;j++){
                    //scopy = new GameStatus(s);
                    if (j == 0 && i == 0 && movAct != null){
                        
                        movPosi.remove(movAct);
                    }else{
                        movAct = movPosi.remove(0);
                    }
                    ////////////////////////////////////////
                    GameStatus scopy2 = new GameStatus(ps);//es pot fer sense new? No, no es pot
                    ////////////////////////////////////////
//                        
                            scopy2.movePiece(fromAct, movAct);
                            long haux = actualizaHash(h, fromAct, movAct, ps);
                            value = Math.max(value, movMin(scopy2, movAct , pprof -1,alpha,beta,haux));
//                            if (beta<= value){
//                                
//                            }
                            if(value>alpha){
                                mv = new Move(fromAct,movAct,numNodes,maxDepth,SearchType.MINIMAX_IDS);
                                alpha = value;
                            }
                        if(alpha>=beta)
                        {
                            break;
                        } 
                }   
              
            }
        }
        tagHASH th = new tagHASH(h,true,alpha,maxDepth,mv,false,alpha,maxDepth,mv);
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

    public int movMin(GameStatus ps,Point lastPoint, int pprof,int alpha, int beta,long h){///Mirar parametros
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
        CellType enemycolor = CellType.opposite(color);
        int numFitxes = ps.getNumberOfPiecesPerColor(enemycolor);
        ArrayList<Point> fitxesPos = new ArrayList<>();
        for (int i = 0; i < numFitxes; i++) {
            //desem on estan totes les fitxes
            fitxesPos.add(ps.getPiece(enemycolor, i));
        }
        
        Move mv=new Move(lastPoint,lastPoint,0,0,SearchType.MINIMAX_IDS);
        Point fromAct = null;
        Point movAct = null;
        //long h = hash;
        //ens assegurem de que si el mapa conte el tauler, que sigui el correcte
        if (mapa.containsKey(h) && mapa.get(h) != null && mapa.get(h).isMin 
                && fitxesPos.contains(mapa.get(h).bestMin.getFrom())
                && ps.getMoves(mapa.get(h).bestMin.getFrom()).contains(mapa.get(h).bestMin.getTo())){
            fromAct = mapa.get(h).bestMin.getFrom();
            fitxesPos.remove(fromAct);
            movAct = mapa.get(h).bestMin.getTo();
            if(alpha<mapa.get(h).heurMin)alpha= mapa.get(h).heurMin;
            mv = new Move(fromAct,movAct,numNodes,maxDepth,SearchType.MINIMAX_IDS);
        }
        for(int i = 0; i < numFitxes; i++){
            //la h no caldria que la calcules a cada iteració, es podria fer eficient
            //comencem pel millor, en cas de que existeixi
            if (i >= 1 || fromAct == null){
                fromAct = fitxesPos.remove(0);
            }                   
                        ArrayList<Point> movPosi = ps.getMoves(fromAct); 
            if(movPosi.size() > 0 ){
                for (int j=0;movPosi.size()>0;j++){
                    //scopy = new GameStatus(s);
                    if (j == 0 && i == 0 && movAct != null){
                        
                        movPosi.remove(movAct);
                    }else{
                        movAct = movPosi.remove(0);
                    }
                    ////////////////////////////////////////
                    GameStatus scopy2 = new GameStatus(ps);//es pot fer sense new? No, no es pot
                    ////////////////////////////////////////
//                        
                            scopy2.movePiece(fromAct, movAct);
                            long haux = actualizaHash(h, fromAct, movAct, ps);
                            value = Math.min(value, movMax(scopy2, movAct , pprof -1,alpha,beta,haux));
                            if(value<beta){
                                mv = new Move(fromAct,movAct,numNodes,maxDepth,SearchType.MINIMAX_IDS);
                                beta = value;
                            }
                        if(alpha>=beta)
                        {
                            break;
                        } 
                }   
              
            }
        }
        tagHASH th = new tagHASH(h,false,alpha,maxDepth,mv,true,beta,maxDepth,mv);
        AddToHASH(ps,th,h);
        return beta;
//        int value = Integer.MAX_VALUE;
//        
//        CellType colorRival = CellType.opposite(color);
//        
//        int numFitxes = ps.getNumberOfPiecesPerColor(colorRival);
//        
//        ArrayList<Point> fitxesPos = new ArrayList<>();
//        for (int i = 0; i < numFitxes; i++) {
//            //desem on estan totes les fitxes
//            fitxesPos.add(ps.getPiece(colorRival, i));
//        }
//       
//        
//        for(int i = 0; i < numFitxes; i++){
//            Point fromAct = fitxesPos.remove(0);
//            //suponemos que el remove actualiza las posiciones del array
//            ArrayList<Point> movPosi = ps.getMoves(fromAct);
//            
//            if(movPosi.size() > 0 ){
//                for (int j=0;j<movPosi.size();j++){
//                    GameStatus scopy2 = new GameStatus(ps);
//                    
//                    
//                    //Zobrist
////                    int x = GetHashTauler(scopy2);
////                        if(mapa.containsKey(x) && mapa.get(x)!=null){
////                            tagHASH th = mapa.get(x);
////                            if(movPosi.contains(th.best.getTo())){
////                               
////                               movPosi.remove(th.best.getTo());
////                            }
////                            value = Math.min(value, movMax(scopy2, th.best.getTo() ,pprof -1,alpha,beta));
////                        }else{
//                            Point movAct = movPosi.remove(0);
//                            scopy2.movePiece(fromAct, movAct);//Movemos la pieza
//                            long haux = actualizaHash(hash, fromAct, movAct, ps);
//                            value = Math.min(value, movMax(scopy2, movAct ,pprof-1,alpha,beta,haux));
////                        }
//                         //Fi codi Zobrist
//                    beta = Math.min(value,beta);
//                    if(alpha>=beta)
//                    {
//                        break;
//                    }                     
//                }   
//            }
//        }
//        
//        return beta;
    }
        //algorithm steps:
    //1.Al inici de la partida, generar una array de 12*64 
    private int AddToHASH(GameStatus ps,tagHASH th,long hash){
        int added = 0;
        long hTauler = hash;
        tagHASH other = mapa.get(hTauler);
        tagHASH add = other;
        if (other == null){
            added = 1;
            mapa.put(hTauler,th);
        }else
        {
            //si el que hi havia no tenia MAX i el nostre si o si era pitjor, actualitzem
            if(!other.isMax && th.isMax || (other.isMax && th.isMax && (th.profMax > other.profMax ||
                    (th.profMax == other.profMax && th.heurMax > other.heurMax)))){
                add.heurMax = th.heurMax;
                add.profMax = th.profMax;
                add.bestMax = th.bestMax;
                added = 1;
            }
            //si el que hi havia no tenia MIN i el nostre si o si era pitjor, actualitzem
            if(!other.isMin && th.isMin || (other.isMin && th.isMin && (th.profMin > other.profMin ||
                    (th.profMin == other.profMin && th.heurMin > other.heurMin)))){
                add.heurMin = th.heurMin;
                add.profMin = th.profMin;
                add.bestMin = th.bestMin;
                added = 1;
            }
            //si hem modificat el min el max o ambdos, afegim
            if (added==1) mapa.put(hTauler,add);
        
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
        numNodes++;
        int valorHeur = -10*Math.abs(ps.getNumberOfPiecesPerColor(color)-ps.getNumberOfPiecesPerColor(CellType.opposite(color)));
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (ps.getPos(i,j) == color) {
                    valorHeur += tablaPuntuacio[i][j];
                } else if (ps.getPos(i,j) == CellType.opposite(color )) {
                    valorHeur -= tablaPuntuacio[i][j];
                }
            }
        }
        return valorHeur + 2*BlockHeur(ps) + 4*DistHeur(ps);

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


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
  