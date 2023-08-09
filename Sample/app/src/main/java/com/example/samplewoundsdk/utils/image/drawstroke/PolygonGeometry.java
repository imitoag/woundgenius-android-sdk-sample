package com.example.samplewoundsdk.utils.image.drawstroke;

import android.graphics.Point;
import android.util.SparseArray;

import com.example.samplewoundsdk.data.pojo.measurement.MeasurementMetadata;

import java.util.ArrayList;
import java.util.List;

public class PolygonGeometry {
    //method fo calculating distance between 2 points
    public static double calculateDistance(Point start, Point end) {
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double qX = Math.pow(dx, 2);
        double qY = Math.pow(dy, 2);
        double squaredDistance = qX + qY;
        return Math.sqrt(squaredDistance);
    }

    public static double calculateDistanceMeasurePoints(MeasurementMetadata.Point start, MeasurementMetadata.Point end) {
        return calculateDistance(new Point(start.getX(), start.getY()), new Point(end.getX(), end.getY()));
    }

    // calculating length of a polygon
    public PolygonDistance calculateLength(List<Point> points) {
        double longestDistance = 0;
        int vertexSize = points.size();
        int startPointIndex = 0;
        int endPointIndex = 0;
        for (int i = 0; i < vertexSize; i++) {
            Point startPoint = points.get(i);
            if (i < vertexSize - 1) {
                for (int j = i + 1; j < vertexSize; j++) {
                    Point endPoint = points.get(j);
                    double distance = calculateDistance(startPoint, endPoint);
                    if (distance > longestDistance) {
                        longestDistance = distance;
                        startPointIndex = i;
                        endPointIndex = j;
                    }
                }
            }
        }
        return new PolygonDistance(longestDistance, startPointIndex, endPointIndex);
    }

    private List<Point> convertPointsToMeasurePoints(List<MeasurementMetadata.Point> points) {
        List<Point> pointList = new ArrayList<>();
        for (MeasurementMetadata.Point point : points) {
            pointList.add(new Point(point.getX(), point.getY()));
        }
        return pointList;
    }

    public PolygonDistance calculateLengthMeasurePoints(List<MeasurementMetadata.Point> points) {
        return calculateLength(convertPointsToMeasurePoints(points));
    }

    //method for spliting polygon on 2 halves, by length of polygon
    private double splitSides(List<Point> points, int startDiagonalIndex, int endDiagonalIndex, ArrayList<Point> firstSidePoints, ArrayList<Point> secondSidePoints) {
        double distanceToIncreasePerpendecularFor;
        double fFirstSideDistance = 0;
        double fSecondSideDistance = 0;
        if (endDiagonalIndex > startDiagonalIndex) {
            firstSidePoints.add(points.get(startDiagonalIndex));
            firstSidePoints.add(points.get(endDiagonalIndex));

            secondSidePoints.add(points.get(endDiagonalIndex));
            secondSidePoints.add(points.get(startDiagonalIndex));

            for (int i = startDiagonalIndex + 1; i < endDiagonalIndex; i++) {
                firstSidePoints.add(firstSidePoints.size() - 1, points.get(i));
            }

            for (int i = endDiagonalIndex + 1; i < points.size(); i++) {
                secondSidePoints.add(secondSidePoints.size() - 1, points.get(i));
            }
            for (int i = 0; i < startDiagonalIndex; i++) {
                secondSidePoints.add(secondSidePoints.size() - 1, points.get(i));
            }

            for (int i = 1; i < firstSidePoints.size() - 1; i++) {
                Point point = firstSidePoints.get(i);
                double distance = distanceToLine(point, points.get(startDiagonalIndex), points.get(endDiagonalIndex));
                double abs = Math.abs(distance);
                if (fFirstSideDistance < abs) {
                    fFirstSideDistance = abs;
                }
            }

            for (int i = 1; i < secondSidePoints.size() - 1; i++) {
                Point point = secondSidePoints.get(i);
                double distance = distanceToLine(point, points.get(startDiagonalIndex), points.get(endDiagonalIndex));
                double abs = Math.abs(distance);
                if (fSecondSideDistance < abs) {
                    fSecondSideDistance = abs;
                }
            }
            distanceToIncreasePerpendecularFor = fFirstSideDistance + fSecondSideDistance + 1;
            return distanceToIncreasePerpendecularFor;
        } else throw new RuntimeException("End index must be less then start");
    }

    //
    private Point pointOnLineFromDroppedPerpendicular(Point point, Point endPoint, Point startPoint) {
        double px = endPoint.x - startPoint.x;
        double py = endPoint.y - startPoint.y;
        double dAB = px * px + py * py;
        double u = ((point.x - startPoint.x) * px + (point.y - startPoint.y) * py) / dAB;
        double x = startPoint.x + u * px;
        double y = startPoint.y + u * py;
        return new Point((int) x, (int) y);
    }

    private Point increaseLengthOfLineToPoint(Point self, Point linePoint2, double distance) {
        double dx = linePoint2.x - self.x;
        double dy = linePoint2.y - self.y;
        double k = Math.sqrt(distance * distance / (dx * dx + dy * dy));
        double x = self.x + dx * k;
        double y = self.y + dy * k;
        return new Point((int) x, (int) y);
    }

    private Point getLinesIntersection(Point firstLineP0, Point firstLineP1, Point secondLineP0, Point secondLineP1) {
        double s1_x = firstLineP1.x - firstLineP0.x;
        double s1_y = firstLineP1.y - firstLineP0.y;
        double s2_x = secondLineP1.x - secondLineP0.x;
        double s2_y = secondLineP1.y - secondLineP0.y;
        double s = (-s1_y * (firstLineP0.x - secondLineP0.x) + s1_x * (firstLineP0.y - secondLineP0.y)) / (-s2_x * s1_y + s1_x * s2_y);
        double t = (s2_x * (firstLineP0.y - secondLineP0.y) - s2_y * (firstLineP0.x - secondLineP0.x)) / (-s2_x * s1_y + s1_x * s2_y);
        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {

            double i_x = firstLineP0.x + (t * s1_x);
            double i_y = firstLineP0.y + (t * s1_y);
            return new Point((int) i_x, (int) i_y);
        }
        return null;
    }

    //
    public PolygonWidth calculateWidth(List<Point> points, int startDiagonalIndex, int endDiagonalIndex) {
        //split points to two sides
        ArrayList<Point> firstSidePoints = new ArrayList<>();
        ArrayList<Point> secondSidePoints = new ArrayList<>();
        double distanceToIncreasePerpendecularFor = splitSides(points, startDiagonalIndex, endDiagonalIndex, firstSidePoints, secondSidePoints);
        //TODO calculate width
        double maximalFoundWidth = 0;
        Point maximalWidthStartPoint = new Point();
        Point maximalWidthEndPoint = new Point();
        for (int i = 1; i < firstSidePoints.size() - 1; i++) {

            Point perpendicularPoint = pointOnLineFromDroppedPerpendicular(firstSidePoints.get(i), points.get(startDiagonalIndex), points.get(endDiagonalIndex));
            Point segmentEndPoint = increaseLengthOfLineToPoint(firstSidePoints.get(i), perpendicularPoint, distanceToIncreasePerpendecularFor);

            for (int j = 0; j < secondSidePoints.size() - 1; j++) {
                Point firstPointLine1 = firstSidePoints.get(i);
                Point secondPointLine1 = segmentEndPoint;
                Point firstPointLine2 = secondSidePoints.get(j);
                Point secondPointLine2 = secondSidePoints.get(j + 1);
                Point linesIntersectionPoint = getLinesIntersection(firstPointLine1, secondPointLine1, firstPointLine2, secondPointLine2);
                if (linesIntersectionPoint != null) {
                    double distanceBetweenStartAndIntersection = calculateDistance(firstSidePoints.get(i), linesIntersectionPoint);
                    if (distanceBetweenStartAndIntersection > maximalFoundWidth) {
                        maximalFoundWidth = distanceBetweenStartAndIntersection;
                        maximalWidthStartPoint = firstSidePoints.get(i);
                        maximalWidthEndPoint = linesIntersectionPoint;
                    }
                }
            }
        }
        for (int i = 1; i < secondSidePoints.size() - 1; i++) {

            Point perpendicularPoint = pointOnLineFromDroppedPerpendicular(secondSidePoints.get(i), points.get(startDiagonalIndex), points.get(endDiagonalIndex));
            Point segmentEndPoint = increaseLengthOfLineToPoint(secondSidePoints.get(i), perpendicularPoint, distanceToIncreasePerpendecularFor);
            for (int j = 0; j < firstSidePoints.size() - 1; j++) {
                Point linesIntersectionPoint = getLinesIntersection(secondSidePoints.get(i), segmentEndPoint, firstSidePoints.get(j), firstSidePoints.get(j + 1));
                if (linesIntersectionPoint != null) {
                    double distanceBetweenStartAndIntersection = calculateDistance(secondSidePoints.get(i), linesIntersectionPoint);
                    if (distanceBetweenStartAndIntersection > maximalFoundWidth) {
                        maximalFoundWidth = distanceBetweenStartAndIntersection;
                        maximalWidthStartPoint = secondSidePoints.get(i);
                        maximalWidthEndPoint = linesIntersectionPoint;
                    }
                }
            }
        }
        Point nearestPoint = findNearestPoint(points, maximalWidthEndPoint);
        return new PolygonWidth(maximalFoundWidth, points.indexOf(maximalWidthStartPoint), points.indexOf(nearestPoint));
    }

    public PolygonWidth calculateWidthMeasurePoints(List<MeasurementMetadata.Point> points, int startDiagonalIndex, int endDiagonalIndex) {
        return calculateWidth(convertPointsToMeasurePoints(points), startDiagonalIndex, endDiagonalIndex);
    }

    private Point findNearestPoint(List<Point> vertices, Point position) {
        SparseArray<Point> distances = new SparseArray<>();
        Point nearestPoint;
        for (int i = 0; i < vertices.size(); i++) {
            Point point = vertices.get(i);
            int calculateDistance = (int) PolygonGeometry.calculateDistance(point, position);
            distances.put(calculateDistance, point);

        }
        int keyAt = distances.keyAt(0);
        nearestPoint = distances.get(keyAt, null);
        return nearestPoint;
    }


    private double distanceToLine(Point point, Point startLinePoint, Point endLinePoint) {
        double top = (endLinePoint.y - startLinePoint.y) * point.x - (endLinePoint.x - startLinePoint.x) * point.y + endLinePoint.x * startLinePoint.y - endLinePoint.y * startLinePoint.x;
        double bottomSquare = (endLinePoint.y - startLinePoint.y) * (endLinePoint.y - startLinePoint.y) + (endLinePoint.x - startLinePoint.x) * (endLinePoint.x - startLinePoint.x);
        double bottom = Math.sqrt(bottomSquare);
        return top / bottom;

    }

    public static class PolygonDistance {
        public double distance;
        public int startPointIndex;
        public int endPointIndex;

        public PolygonDistance(double distance) {
            this.distance = distance;
        }

        public PolygonDistance(double distance, int startPointIndex, int endPointIndex) {
            this(distance);
            this.startPointIndex = startPointIndex;
            this.endPointIndex = endPointIndex;
        }
    }

    public static class PolygonWidth {
        public double distance;
        public int startPointIndex;
        public int endPointIndex;

        public PolygonWidth(double distance) {
            this.distance = distance;
        }

        public PolygonWidth(double distance, int startPointIndex, int endPointIndex) {
            this(distance);
            this.startPointIndex = startPointIndex;
            this.endPointIndex = endPointIndex;
        }
    }
}