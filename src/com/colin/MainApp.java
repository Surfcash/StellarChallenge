package com.colin;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class MainApp extends PApplet {

    public static void main(String[] args) {
        String[] PApp = {"com.colin.MainApp"};
        PApplet.main(PApp);
    }

    System system;
    boolean paused;
    boolean orbits;

    public void setup() {
        surface.setTitle("Colin's Solar Systems");
        surface.setResizable(false);
        surface.setLocation(-3, -3);
        system = new System();
        paused = false;
        orbits = false;
        frameRate(60);
        background(0);
    }

    public void settings() {
        size(displayWidth, displayHeight - 61);
    }

    public void draw() {
        drawRectBackground();
        if(!paused) {
            system.update();
        } else {
            system.renderBodyIndicator();
        }

        system.render();
        system.renderGUI();
        if(paused) {
            system.renderPausedGUI();
        }
    }

    void drawRectBackground() {
        noStroke();
        if(orbits) {
            fill(0, 0, 0, 6);
        } else {
            fill(0);
        }
        rect(0, 0, width, height);
    }

    public void keyPressed() {
        println(keyCode);
        if(keyCode == 32) {
            paused = !paused;
            background(0);
            draw();
        } else if(keyCode == 79) {
            orbits = !orbits;
        }
    }

    class System {
        private ArrayList<Body> bodies;
        private Body selectedBody;

        System() {
            bodies = new ArrayList();
            bodies.add(new Star(0, 0));
            genPlanets();
            printBodies();
        }

        void render() {
            renderBodies();
        }

        void update() {
            updateBodies();
            checkStarCollision();
        }

        void renderGUI() {
            stroke(40);
            strokeWeight(3);
            fill(75, 75, 75, 180);
            rect(20, 10, 190, 55);

            stroke(255);
            strokeWeight(1);
            textAlign(LEFT);
            textSize(15);
            fill(255);
            text("'O' - Toggle Orbit Paths\n'Space' - Toggle Pause", 30, 30);
        }

        void renderPausedGUI() {
            stroke(40);
            strokeWeight(3);
            fill(75, 75, 75, 180);
            rect(0, height - 50, width, 50);
            rect(width / 2F - 75, height - 100, 150, 50);
            textAlign(CENTER, CENTER);
            fill(255);
            stroke(255);
            if(getHoveredBody() != null) {
                if(mouseButton == LEFT) {
                    setSelectedBody(getHoveredBody());
                }
                if(getHoveredBody() != getSelectedBody()) {
                    textSize(15);
                    text(getHoveredBody().toString(), width / 2F, height - 25);
                    textSize(35);
                    if(getHoveredBody() instanceof Star) {
                        text("STAR", width / 2F, height - 80);
                    } else if(getHoveredBody() instanceof Planet) {
                        text("PLANET", width / 2F, height - 80);
                    }
                    setSelectedBody(null);
                }
            }
            if(getSelectedBody() != null) {
                textSize(15);
                text(getSelectedBody().toString(), width / 2F, height - 25);
                textSize(35);
                if(getSelectedBody() instanceof Star) {
                    text("STAR", width / 2F, height - 80);
                } else if(getSelectedBody() instanceof Planet) {
                    text("PLANET", width / 2F, height - 80);
                }
            }
            if(getHoveredBody() == null && getSelectedBody() == null) {
                textSize(20);
                text("Hover Over Or Click On An Object For More Info", width / 2F, height - 25);
            }
        }

        private void renderBodies() {
            for(Body i : bodies) {
                i.render();
            }
        }

        private void updateBodies() {
            for(Body i : bodies) {
                i.update();
            }
            applyGravity();
        }

        void renderBodyIndicator() {
            for(Body i : bodies) {
                i.renderHoverIndicator();
            }
        }

        private void applyGravity() {
            for(Body i : bodies) {
                for(Body j : bodies) {
                    if(i != j) {
                        i.modVel((StellarPhysics.gravitationalPull(i, j)));
                    }
                }
            }
        }

        ArrayList getBodies() {
            return bodies;
        }

        void addBody(Body body) {
            bodies.add(body);
        }

        private void printBodies() {
            for(Body i : bodies) {
                if(i instanceof Star) {
                    println("- Star -");
                } else if(i instanceof Planet) {
                    println("- Planet -");
                } else {
                    println("- Body -");
                }
                println(i.toString() + "\n");
            }
        }

        private void genPlanets() {
            int rand = floor(random(1, 14));
            Star star = (Star)bodies.get(0);
            for(int i = 0; i < rand; i++) {
                int randX = (random(1) < 0.5) ? 1 : -1;
                int randY = (random(1) < 0.5) ? 1 : -1;
                println("Random for " + i + ": " + randX + ", " + randY);
                bodies.add(new Planet(((floor(random(width / 3F) * randX) + (randX * (star.getPixelRadius() / 2)))), (floor(random(width / 3F) * randY) + (randY * (star.getPixelRadius() / 2))), star));
            }
        }

        private void checkStarCollision() {
            BaseLoop : for(Body i : bodies) {
                for(Body j : bodies) {
                    if(j instanceof Star && !(i instanceof Star)) {
                        if(StellarPhysics.pointInsideBody(i.getPos(), j)) {
                            println("COLLISION");
                            bodies.remove(i);
                            break BaseLoop;
                        }
                    }
                }
            }
        }

        private Body getHoveredBody() {
            for(Body i : bodies) {
                if(i.checkForHover()) {
                    return i;
                }
            }
            return null;
        }

        private Body getSelectedBody() {
            return selectedBody;
        }

        private void setSelectedBody(Body body) {
            this.selectedBody = body;
        }
    }

    class Body {
        private double mass;
        private double density;
        private double radius;
        private int pixelRadius;
        private PVector vel;
        private PVector pos;
        private int chromaticity;
        private PGraphics gfx;
        private PGraphics hovergfx;

        Body() {
            this.mass = 0;
            this.density = 0;
            this.radius = 0;
            this.pixelRadius = 1;
            this.vel = new PVector(0, 0);
            this.pos = new PVector(0, 0);
            this.chromaticity = 255;
        }

        Body(double mass, double density, double radius, PVector vel, PVector pos) {
            this.mass = mass;
            this.density = density;
            this.radius = radius;
            this.vel = new PVector(vel.x, vel.y);
            this.pos = new PVector(pos.x, pos.y);
            this.chromaticity = 100;
            calcPixelRadius();
            initGFX();
        }

        void render() {
            renderBody();
        }

        void update() {
            applyVelocity();
        }

        private void renderBody() {
            pushMatrix();
            translate(width / 2F, height / 2F);
            imageMode(CENTER);
            image(getGFX(), getPos().x, getPos().y);
            popMatrix();
        }

        void renderHoverIndicator() {
            if(checkForHover()) {
                println("Hovered");
                pushMatrix();
                translate(width / 2F, height / 2F);
                imageMode(CENTER);
                image(hovergfx, getPos().x, getPos().y);
                popMatrix();
            }
        }

        void initGFX() {
            gfx = createGraphics(getPixelRadius() * 4, getPixelRadius() * 4);
            gfx.beginDraw();
            gfx.background(0, 0, 0, 0);
            gfx.strokeWeight(getPixelRadius() * 2);
            gfx.stroke(getChroma());
            gfx.point(gfx.width / 2, gfx.height / 2);
            gfx.endDraw();

            hovergfx = createGraphics(getPixelRadius() * 4, getPixelRadius() * 4);
            hovergfx.beginDraw();
            hovergfx.strokeWeight(getPixelRadius() * 2 + 5);
            hovergfx.stroke(255, 0, 0);
            hovergfx.point(hovergfx.width / 2, hovergfx.height / 2);
            hovergfx.endDraw();
        }

        PGraphics getGFX() {
            return gfx;
        }

        void setGFX(PGraphics gfx) {
            this.gfx = gfx;
        }

        PGraphics getHoverGFX() {
            return hovergfx;
        }

        void setHoverGFX(PGraphics gfx) {
            this.hovergfx = gfx;
        }

        double getMass() {
            return mass;
        }

        void setMass(double mass) {
            this.mass = mass;
        }

        double getDensity() {
            return density;
        }

        void calcDensity() {
            double volume = (4 / 3) * PI * pow((float)radius * 100, 3);
            setDensity(mass * 1000 / volume);
        }

        void setDensity(double density) {
            this.density = density;
        }

        double getRadius() {
            return radius;
        }

        void setRadius(double radius) {
            this.radius = radius;
        }

        int getPixelRadius() {
            return pixelRadius;
        }

        void calcPixelRadius() {
            this.pixelRadius = StellarPhysics.pixelUnit((float)getRadius());
        }

        PVector getPos() {
            return pos;
        }

        void setPos(PVector pos) {
            this.pos = new PVector(pos.x, pos.y);
        }

        PVector getVel() {
            return vel;
        }

        void setVel(PVector vel) {
            this.vel = new PVector(vel.x, vel.y);
        }

        void modVel(PVector delta) {
            this.vel = this.vel.add(delta);
        }

        private void applyVelocity() {
            pos.add(vel);
        }

        boolean checkForHover() {
            return StellarPhysics.pointInsideBody(new PVector(mouseX - width / 2F, mouseY - height / 2F), this);
        }

        int getChroma() {
            return chromaticity;
        }

        void setChroma(int chromaticity) {
            this.chromaticity = chromaticity;
        }

        public String toString() {
            return "Mass(kg): " + mass + "     Density(g/m3): " + density + "     Radius(km): " + radius;
        }
    }

    class Planet extends Body {
        private String classification;

        Planet() {
            super();
            randomClass();
            setChroma(color(floor(random(50, 100)), floor(random(50, 100)), floor(random(50, 100))));
        }

        Planet(float x, float y, Star star) {
            this();
            setPos(new PVector(x, y));
            modVel(StellarPhysics.stableOrbitInertia(this, star));
            initGFX();
        }

        Planet(double mass, double density, float radius, PVector vel, PVector pos) {
            super(mass, density, radius, vel, pos);
            classification = "Unknown";
            initGFX();
        }

        void render() {
            super.render();
        }

        void update() {
            super.update();
        }

        private void initClass(String classification) {
            this.classification = classification;
            float rand = random(1);
            switch(classification) {
                case "Jovian": {
                    setMass(StellarPhysics.convertEarthMass(map(rand, 0, 1, 50.0F, 75.0F)));
                    setRadius(StellarPhysics.convertEarthRadii(map(rand, 0, 1, 6.0F, 9.0F)));
                    break;
                }
                case "Superterran": {
                    setMass(StellarPhysics.convertEarthMass(map(rand, 0, 1, 5.0F, 10.0F)));
                    setRadius(StellarPhysics.convertEarthRadii(map(rand, 0, 1, 1.5F, 2.5F)));
                    break;
                }
                case "Neptunian": {
                    setMass(StellarPhysics.convertEarthMass(map(rand, 0, 1, 10.0F, 50.0F)));
                    setRadius(StellarPhysics.convertEarthRadii(map(rand, 0, 1, 2.5F, 6.0F)));
                    break;
                }
                case "Terran": {
                    setMass(StellarPhysics.convertEarthMass(map(rand, 0, 1, 0.5F, 5.0F)));
                    setRadius(StellarPhysics.convertEarthRadii(map(rand, 0, 1, 0.8F, 1.5F)));
                    break;
                }
                case "Subterran": {
                    setMass(StellarPhysics.convertEarthMass(map(rand, 0, 1, 0.5F, 5.0F)));
                    setRadius(StellarPhysics.convertEarthRadii(map(rand, 0, 1, 0.8F, 1.5F)));
                    break;
                }
                case "Miniterran": {
                    setMass(StellarPhysics.convertEarthMass(map(rand, 0, 1, 0.5F, 5.0F)));
                    setRadius(StellarPhysics.convertEarthRadii(map(rand, 0, 1, 0.8F, 1.5F)));
                    break;
                }
                default : {
                }
            }
            calcDensity();
            calcPixelRadius();
        }

        private void randomClass() {
            float rand = random(1);
            String classification;
            if(rand < 0.328){
                classification = "Jovian";
            } else if(rand < 0.59) {
                classification = "Superterran";
            } else if(rand < 0.806) {
                classification = "Neptunian";
            } else if(rand < 0.973) {
                classification = "Terran";
            } else if(rand < 0.988) {
                classification = "Subterran";
            } else {
                classification = "Miniterran";
            }
            initClass(classification);
            initClass(classification);
        }

        public String toString() {
            return super.toString() + "     Planet Classification: " + classification;
        }
    }

    class Star extends Body {
        private char classification;
        private double tempK;

        Star() {
            super();
            randomClass();
        }

        Star(float x, float y) {
            this();
            setPos(new PVector(x, y));
        }

        Star(double mass, double density, float radius, PVector vel, PVector pos, double tempK) {
            super(mass, density, radius, vel, pos);
            this.classification = '0';
            this.tempK = tempK;
            initGFX();
        }

        Star(char classification) {
            super();
            initClass(classification);
            initGFX();
        }

        void render() {
            super.render();
        }

        void update() {
            super.update();
        }

        void initGFX() {
            PGraphics gfx = createGraphics(floor(getPixelRadius() * 4), floor(getPixelRadius() * 4));
            gfx.beginDraw();
            gfx.strokeWeight(getPixelRadius() * 2 + 4);
            gfx.stroke(getChroma());
            gfx.point(gfx.width / 2, gfx.height / 2);
            gfx.filter(BLUR, 6);
            gfx.strokeWeight(getPixelRadius() * 2 - 4);
            gfx.point(gfx.width / 2, gfx.height / 2);
            gfx.endDraw();
            setGFX(gfx);

            PGraphics hovergfx = createGraphics(getPixelRadius() * 4, getPixelRadius() * 4);
            hovergfx.beginDraw();
            hovergfx.strokeWeight(getPixelRadius() * 2 + 5);
            hovergfx.stroke(255, 0, 0);
            hovergfx.point(gfx.width / 2, gfx.height / 2);
            hovergfx.endDraw();
            setHoverGFX(hovergfx);
        }

        private void initClass(char classification) {
            this.classification = classification;
            float rand = random(1);
            switch(classification) {
                case 'M': {
                    setMass(StellarPhysics.convertSolarMass(map(rand, 0, 1, 0.08F, 0.45F)));
                    setRadius(StellarPhysics.convertSolarRadii(map(rand, 0, 1, 0.2F, 0.7F)));
                    setTemp(map(rand, 0, 1, 2400, 3700));
                    setChroma(color(255, 204, 111));
                    break;
                }
                case 'K': {
                    setMass(StellarPhysics.convertSolarMass(map(rand, 0, 1, 0.45F, 0.8F)));
                    setRadius(StellarPhysics.convertSolarRadii(map(rand, 0, 1, 0.7F, 0.96F)));
                    setTemp(map(rand, 0, 1, 3700, 5200));
                    setChroma(color(255, 210, 161));
                    break;
                }
                case 'G': {
                    setMass(StellarPhysics.convertSolarMass(map(rand, 0, 1, 0.8F, 1.04F)));
                    setRadius(StellarPhysics.convertSolarRadii(map(rand, 0, 1, 0.96F, 1.15F)));
                    setTemp(map(rand, 0, 1, 5200, 6000));
                    setChroma(color(255, 244, 234));
                    break;
                }
                case 'F': {
                    setMass(StellarPhysics.convertSolarMass(map(rand, 0, 1, 1.04F, 1.4F)));
                    setRadius(StellarPhysics.convertSolarRadii(map(rand, 0, 1, 1.15F, 1.4F)));
                    setTemp(map(rand, 0, 1, 6000, 7500));
                    setChroma(color(248, 247, 255));
                    break;
                }
                case 'A': {
                    setMass(StellarPhysics.convertSolarMass( map(rand, 0, 1, 1.4F, 2.1F)));
                    setRadius(StellarPhysics.convertSolarRadii(map(rand, 0, 1, 1.4F, 1.8F)));
                    setTemp(map(rand, 0, 1, 7500, 10000));
                    setChroma(color(202, 215, 255));
                    break;
                }
                case 'B': {
                    setMass(StellarPhysics.convertSolarMass(map(rand, 0, 1, 2.1F, 16.0F)));
                    setRadius(StellarPhysics.convertSolarRadii(map(rand, 0, 1, 1.8F, 6.6F)));
                    setTemp(map(rand, 0, 1, 10000, 30000));
                    setChroma(color(170, 191, 255));
                    break;
                }
                default : {
                }
            }
            calcDensity();
            calcPixelRadius();
            initGFX();
        }

        private void randomClass() {
            float rand = random(1);
            char classification;
            if(rand < 0.7646){
                classification = 'M';
            } else if(rand < 0.8856) {
                classification = 'K';
            } else if(rand < 0.9616) {
                classification = 'G';
            } else if(rand < 0.9916) {
                classification = 'F';
            } else if(rand < 0.9976) {
                classification = 'A';
            } else {
                classification = 'B';
            }
            initClass(classification);
        }

        double getTempK() {
            return tempK;
        }

        void setTemp(double tempK) {
            this.tempK = tempK;
        }

        public String toString() {
            return super.toString() + "     Star Classification: " + classification + "     Temperature(K): " + tempK;
        }
    }

    public static class StellarPhysics {
        static final double GRAVITATIONAL_CONSTANT = 6.6726 * pow(10, -11);
        static final double SOLAR_MASS = 1.989 * pow(10, 30);
        static final double SOLAR_RADIUS = 6.957 * pow(10, 5);
        static final double EARTH_MASS = 5.972 * pow(10, 24);
        static final double EARTH_RADIUS = 6.371 * pow(10, 3);
        static final int PIXEL_UNIT = 4000;

        //Returns PVector representing the velocity of the gravitational pull b2 applies on b1
        static PVector gravitationalPull(Body b1, Body b2) {
            float angle = angleVector(b1.getPos(), b2.getPos());
            double force = ((GRAVITATIONAL_CONSTANT * b1.getMass() * b2.getMass()) / (pow(distVector(b1.getPos(), b2.getPos()), 2) * 1000));
            double vel = -(force / b1.getMass()) / 1000;
            return new PVector(((float)(vel * cos(angle)) / PIXEL_UNIT), ((float)(vel * sin(angle))) / PIXEL_UNIT);
        }

        //Returns float representing angle from p1 to p2
        static float angleVector(PVector p1, PVector p2) {
            return atan2(p1.y - p2.y, p1.x - p2.x);
        }

        //Returns float representing distance from p1 to p2
        static float distVector(PVector p1, PVector p2) {
            return dist(p1.x, p1.y, p2.x, p2.y) * PIXEL_UNIT;
        }

        static int pixelUnit(float kilometers) {
            return floor(kilometers / PIXEL_UNIT);
        }

        static double convertSolarMass(float solarMasses) {
            return solarMasses * SOLAR_MASS;
        }

        static double convertSolarRadii(float solarRadii) {
            return solarRadii * SOLAR_RADIUS;
        }

        static double convertEarthMass(float earthMasses) {
            return earthMasses * EARTH_MASS;
        }

        static double convertEarthRadii(float earthRadii) {
            return earthRadii * EARTH_RADIUS;
        }

        static PVector stableOrbitInertia(Body  b1, Body b2) {
            float angle = angleVector(b1.getPos(), b2.getPos());
            double force = ((GRAVITATIONAL_CONSTANT * b1.getMass() * b2.getMass()) / (pow(distVector(b1.getPos(), b2.getPos()), 2) * 1000));
            double vel = -(force / b1.getMass()) / 1000;
            return new PVector(((float)(vel * cos(angle - radians(90)) * (distVector(b1.getPos(), b2.getPos()) / PIXEL_UNIT / 2)) / PIXEL_UNIT), ((float)(vel * sin(angle - radians(90)) * (distVector(b1.getPos(), b2.getPos()) / PIXEL_UNIT))) / PIXEL_UNIT);
        }

        static boolean pointInsideBody(PVector p1, Body b1) {
            float dist = distVector(p1, b1.getPos());
            return pixelUnit(dist) < b1.getPixelRadius();
        }
    }
}
