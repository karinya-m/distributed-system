package edu.cmu.heinz.ds.pit;

import java.io.Serializable;

/* 
 * A Reset object is passed from PITinit to each Player to signify resetting
 * the Player's state.  The Player then replies with a Reset object, signifying
 * that the reset has been acknowledged.
 */
public class Reset implements Serializable {
}
