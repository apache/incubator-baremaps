package org.apache.baremaps.contour;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;

import javax.swing.*;
import java.awt.*;

public class ChaikinAlgorithm {

    public static Coordinate[] chaikin(Coordinate[] arr, int num, double factor, boolean isOpen) {
        if (arr == null || arr.length < 2 || num <= 0) {
            throw new IllegalArgumentException("Invalid input");
        }

        double f1 = factor;
        double f2 = 1 - factor;

        Coordinate[] result = arr;

        // Apply the algorithm repeatedly
        for (int n = 0; n < num; n++) {
            Coordinate[] temp = new Coordinate[isOpen ? 2 * result.length - 2 : 2 * result.length];

            for (int i = 0; i < result.length; i++) {
                temp[2 * i] = new Coordinate(
                        f1 * result[i].x + f2 * result[(i + 1) % result.length].x,
                        f1 * result[i].y + f2 * result[(i + 1) % result.length].y
                );
                temp[2 * i + 1] = new Coordinate(
                        f2 * result[i].x + f1 * result[(i + 1) % result.length].x,
                        f2 * result[i].y + f1 * result[(i + 1) % result.length].y
                );
            }

            if (isOpen) {
                temp[0] = result[0];
                temp[temp.length - 1] = result[result.length - 1];
            }

            result = temp;
        }

        return result;
    }

    public static void main(String... args) {
        var arr = new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1),
        };
        var result = chaikin(arr, 3, 0.75, false);
        for (var coord : result) {
            System.out.println(coord);
        }

        var frame = new JFrame("Chaikin Algorithm");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setVisible(true);

        var panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                for (int i = 0; i < arr.length; i++) {
                    var coord = arr[i];
                    var next = arr[(i + 1) % arr.length];
                    g.drawLine((int) (coord.x * 100 + 100), (int) (coord.y * 100 + 100), (int) (next.x * 100 + 100), (int) (next.y * 100 + 100));
                }
                g.setColor(Color.RED);
                for (int i = 0; i < result.length; i++) {
                    var coord = result[i];
                    var next = result[(i + 1) % result.length];
                    g.drawLine((int) (coord.x * 100 + 100), (int) (coord.y * 100 + 100), (int) (next.x * 100 + 100), (int) (next.y * 100 + 100));
                }
            }
        };
        frame.add(panel);


    }
}
