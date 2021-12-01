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
/**
 * Jugador humà de LOA
 * @author bernat
 */
public class loser1 implements IPlayer {

    String name;
    CellType color;
    private GameStatus scopy;
    private int numNodes;
    private int maxDepth;

    public loser1(String name) {
        this.name = name;
        numNodes = 0;
        maxDepth = 0;
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
        return null;
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public void timeout() {
        // Bah! Humans do not enjoy timeouts, oh, poor beasts !
        System.out.println("Bah! You are so slow...");
    }

    /**
     * Retorna el nom del jugador que s'utlilitza per visualització a la UI
     *
     * @return Nom del jugador
     */
    @Override
    public String getName() {
        return "Human(" + name + ")";
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
                    scopy = s;
                    Point movAct = movPosi.remove(0);
                    scopy.movePiece(fromAct, movAct);
                    int valorNou = movMin(scopy ,pprof-1,alpha,beta);
                    
                    if(valorNou > valor){
                        valor = valorNou;
                        bestFrom = fromAct;
                        bestTo = movAct;
                        
                    }
                }        
            } 
        }//if (bestFrom.x == -100) return new Move(bestFrom,bestTo,0,maxDepth,SearchType.MINIMAX);
        
        return new Move(bestFrom,bestTo,numNodes,maxDepth,SearchType.MINIMAX);
    }

    /**
     * Función que nos devuelve el movimiento con mayor valor heurístico de todos los
     * movimientos estudiados.
     * @param pt tablero resultante de poner una ficha en una determinada columna.
     * @param lastcol columna en la que hemos puesto ficha para estudiar el tablero.
     * @param pprof número de niveles restantes que le queda al algoritmo por
     * analizar.
     * @param alpha variable que determina el alfa para realizar la poda alfa-beta.
     * @param beta variable que determina la beta para realizar la poda alfa-beta.
     * @return retorna el valor heurístico máximo entre todas las posibilidades
     * comprobadas.
     */
    public int movMax(Tauler pt, int lastcol ,int pprof,int alpha,int beta){

        if(pt.solucio(lastcol,colorNostre*-1)){
            return -1000000;
        }else if(pt.solucio(lastcol,colorNostre)){
               return 1000000;
        }else if (pprof == 0 || !(pt.espotmoure())){
            return Heuristica(pt);
        }

        int value = Integer.MIN_VALUE;
        for(int i = 0; i < 8; i++){
            if(pt.movpossible(i)){

                Tauler move = new Tauler(pt);
                move.afegeix(i,colorNostre);
                value = Math.max(value, movMin(move,i,pprof-1,alpha ,beta));
                alpha = Math.max(value,alpha);
                if(alpha>=beta)
                {
                    break;
                }
             }
        }
        return value;
    }

    /**
     * Función que nos devuelve el movimiento con menor valor heurístico de todos los
     * movimientos estudiados.
     * @param pt tablero resultante de poner una ficha en una determinada columna.
     * @param lastcol columna en la que hemos puesto ficha para estudiar el tablero.
     * @param pprof número de niveles restantes que le queda al algoritmo por
     * analizar.
     * @param alpha variable que determina el alfa para realizar la poda alfa-beta.
     * @param beta variable que determina la beta para realizar la poda alfa-beta.
     * @return retorna el valor heurístico máximo entre todas las posibilidades
     * comprobadas.
     */

    public int movMin(Tauler pt,int lastcol, int pprof,int alpha, int beta){
        if(pt.solucio(lastcol,colorNostre*-1)){
           return -100000;
        }else if(pt.solucio(lastcol,colorNostre)){
               return 100000;
        }else if (pprof == 0 || !(pt.espotmoure())){
            return Heuristica(pt);
        }

        int value = Integer.MAX_VALUE;
        for(int i = 0; i < 8; i++){
            if(pt.movpossible(i)){
                Tauler move = new Tauler(pt);
                move.afegeix(i, colorNostre*-1);
                value = Math.min(value, movMax(move,i,pprof -1,alpha,beta));
                beta = Math.min(value,beta);
                if(alpha>=beta)
                {
                    break;
                }
             }
        }
        return value;
    }
}
