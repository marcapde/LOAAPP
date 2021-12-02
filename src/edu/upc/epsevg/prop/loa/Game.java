package edu.upc.epsevg.prop.loa;

import edu.upc.epsevg.prop.loa.Level;
import edu.upc.epsevg.prop.loa.IPlayer;
import edu.upc.epsevg.prop.loa.players.*;

import javax.swing.SwingUtilities;

/**
 * Lines Of Action: el joc de taula.
 * @author bernat
 */
public class Game {
        /**
     * @param args
     */
    public static void main(String[] args) {
        
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                IPlayer player1 = new HumanPlayer("Octopus");
//                IPlayer player1 = new RandomPlayer("Crazy Chris");
//                IPlayer player2 = new RandomPlayer("Crazy Ivan");
//                    IPlayer player1 = new RandomPlayer("Octopus");
                    IPlayer player2 = new loser1("mamapinga");         
                new Board(player1 , player2, 10, Level.DIFFICULT);
             }
        });
    }
}
