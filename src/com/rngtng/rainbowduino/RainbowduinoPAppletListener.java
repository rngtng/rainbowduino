package com.rngtng.rainbowduino;

/*
A wrapper class to add PApplet Rainbowduino methods

(c) copyright 2010 by rngtng - Tobias Bielohlawek

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General
Public License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330,
Boston, MA  02111-1307  USA
*/

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import processing.core.PApplet;

/**
* A wrapper class to to PApplet into a LaunchpadListerner for generic Listener handling 
*
* @author rngtng - Tobias Bielohlawek
*
*/
public class RainbowduinoPAppletListener implements RainbowduinoListener {

	PApplet app;

	private Method availableMethod;
	private Method unavailableMethod;
	
	
	RainbowduinoPAppletListener(PApplet _app) {
		app = _app;
		getMethods(app);
	}

	protected void getMethods(Object parent) {
		Class[] argsButton = new Class[] {Rainbowduino.class};
		try {
			availableMethod = parent.getClass().getDeclaredMethod( "rainbowuinoAvailable", argsButton);
		} catch (NoSuchMethodException e) {
			// not a big deal if they aren't implemented
		}
		try {
			unavailableMethod = parent.getClass().getDeclaredMethod( "rainbowuinoUnavailable", argsButton);
		} catch (NoSuchMethodException e) {
			// not a big deal if they aren't implemented
		}
    }

	public void rainbowuinoAvailable(Rainbowduino rainbowudino) {
		if (availableMethod == null) return;			
		try {
			availableMethod.invoke(app, new Object[]{ rainbowudino });
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}   
	}

	public void rainbowuinoUnavailable(Rainbowduino rainbowudino) {
		if (unavailableMethod == null) return;			
		try {
			unavailableMethod.invoke(app, new Object[]{ rainbowudino });
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}  		
	}
	
}
