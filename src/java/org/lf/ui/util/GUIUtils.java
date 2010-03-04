package org.lf.ui.util;

import javax.swing.*;
import java.awt.*;

public class GUIUtils {

	public static void createRecommendedButtonMargin(JButton[] buttons) {
		for (int i=0; i < buttons.length; i++) {
			Insets margin = buttons[i].getMargin();
			margin.left = 12;
			margin.right = 12;
			buttons[i].setMargin(margin);
		}
	}
	
	public static void makeSameWidth(JComponent[] components) {
		int[] sizes = new int[components.length];
		for (int i=0; i<sizes.length; i++) {
			sizes[i] = components[i].getPreferredSize().width;
		}

		int maxSizePos = maximumElementPosition(sizes);
		Dimension maxSize = components[maxSizePos].getPreferredSize();
		
		for (int i=0; i<components.length; i++) {
			components[i].setPreferredSize(maxSize);
			components[i].setMinimumSize(maxSize);
			components[i].setMaximumSize(maxSize);
		}
	}


	public static void fixMaxHeightSize(JComponent comp) {
		Dimension size = comp.getPreferredSize();
		size.width = comp.getMaximumSize().width;
		comp.setMaximumSize(size);
	}

	public static void makePreferredSize(JComponent comp) {
		Dimension size = comp.getPreferredSize();
		comp.setMaximumSize(size);
	}

	
	private static int maximumElementPosition(int[] array) {
		int maxPos = 0;
		for (int i=1; i < array.length; i++) {
			if (array[i] > array [maxPos]) maxPos = i;
		}
		return maxPos;
	}

}
