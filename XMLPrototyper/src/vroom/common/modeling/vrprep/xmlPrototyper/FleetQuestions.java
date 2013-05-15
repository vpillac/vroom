package vroom.common.modeling.vrprep.xmlPrototyper;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Contains all questions relevant to the fleet
 * 
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a> 
 */
public class FleetQuestions extends JPanel {

    /**
	 * 
	 */
    private static final long serialVersionUID = -898410797011859620L;

    private int               xCoord           = 2;
    private int               yCoord           = 4;

    private JLabel            q1;
    private JRadioButton      q1_one;
    private JRadioButton      q1_multiple;
    private JLabel            q3;
    private JRadioButton      q3_yes;
    private JRadioButton      q3_no;
    private JLabel            q4;
    private JRadioButton      q4_yes;
    private JRadioButton      q4_no;
    private JLabel            q5;
    private JRadioButton      q5_yes;
    private JRadioButton      q5_no;
    private JLabel            q6;
    private JRadioButton      q6_yes;
    private JRadioButton      q6_no;
    private JLabel            q7;
    private JRadioButton      q7_yes;
    private JRadioButton      q7_no;
    private JLabel            q8;
    private JCheckBox         q8_average;
    private JCheckBox         q8_intervals;
    private JLabel            q9;
    private JRadioButton      q9_yes;
    private JRadioButton      q9_no;
    private JLabel            q10;
    private JRadioButton      q10_yes;
    private JRadioButton      q10_no;
    private JLabel            q11;
    private JCheckBox         q11_start;
    private JCheckBox         q11_end;
    private JLabel            q12;
    private JRadioButton      q12_yes;
    private JRadioButton      q12_no;
    private JLabel            q13;
    private JRadioButton      q13_yes;
    private JRadioButton      q13_no;
    private JLabel            q14;
    private JRadioButton      q14_yes;
    private JRadioButton      q14_no;
    private JLabel            q15;
    private JRadioButton      q15_yes;
    private JRadioButton      q15_no;
    private JLabel            q16;
    private JRadioButton      q16_yes;
    private JRadioButton      q16_no;
    private JLabel            q17;
    private JRadioButton      q17_yes;
    private JRadioButton      q17_no;
    private JLabel            q18;
    private JRadioButton      q18_yes;
    private JRadioButton      q18_no;
    private JLabel            q19;
    private JRadioButton      q19_yes;
    private JRadioButton      q19_no;
    private JLabel            q20;
    private JRadioButton      q20_yes;
    private JRadioButton      q20_no;
    private JLabel            q21;
    private JRadioButton      q21_yes;
    private JRadioButton      q21_no;
    private JLabel            q22;
    private JRadioButton      q22_yes;
    private JRadioButton      q22_no;
    private JLabel            q23;
    private JCheckBox         q23_start;
    private JCheckBox         q23_end;
    private JLabel            q24;
    private JRadioButton      q24_yes;
    private JRadioButton      q24_no;
    private JLabel            q25;
    private JRadioButton      q25_yes;
    private JRadioButton      q25_no;
    private JLabel            q26;
    private JRadioButton      q26_yes;
    private JRadioButton      q26_no;
    private JLabel            q28;
    private JRadioButton      q28_yes;
    private JRadioButton      q28_no;
    private JLabel            q29;
    private JCheckBox         q29_minMax;
    private JCheckBox         q29_fixed;
    private JLabel            q30;
    private JRadioButton      q30_yes;
    private JRadioButton      q30_no;
    private JLabel            q31;
    private JRadioButton      q31_yes;
    private JRadioButton      q31_no;
    private JLabel            q32;
    private JRadioButton      q32_yes;
    private JRadioButton      q32_no;
    private JLabel            q33;
    private JRadioButton      q33_yes;
    private JRadioButton      q33_no;
    private JLabel            q101;
    private JRadioButton      q101_yes;
    private JRadioButton      q101_no;
    private JLabel            lblWLPAny        = new JLabel(
                                                       "<html><br />Please enter the names of the elements that need to be created :");
    private AnyList           wLPAny;
    
    private JLabel            q105;
    private JRadioButton      q105_yes;
    private JRadioButton      q105_no;
    private JLabel            lblAnyMore        = new JLabel(
                                                       "<html><br />Please enter the names of the elements that need to be created :");
    private AnyList           anyMoreAny;
    private JLabel 			  lbl11;

    private JLabel            lbl1;
    private JLabel            lbl2;
    private JLabel            lbl3;
    private JLabel            lbl4;
    private JLabel            lbl10;
    
    private int qNum = 1;

    /**
     * Create the panel.
     */
    public FleetQuestions() {

        RowSpec[] rowSpec = new RowSpec[240];
        rowSpec[0] = FormFactory.RELATED_GAP_ROWSPEC;
        for (int i = 1; i < rowSpec.length; i++) {
            rowSpec[i] = FormFactory.DEFAULT_ROWSPEC;
        }

        FormLayout fl_this = new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("left:max(60dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, }, rowSpec);
        fl_this.setColumnGroups(new int[][] { new int[] { 12, 10, 8, 6, 4, 2 } });
        setLayout(fl_this);

        addFleetQuestions();
    }

    /**
     * Construct fleet questions
     */
    private void addFleetQuestions() {
        /*
         * 
         * 
         */
        lbl1 = new JLabel("<html><br />Fleet Characteristics :<br />");
        lbl1.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        this.add(lbl1, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q1 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Is the fleet heterogeneous (Are there several different vehicle types)?");
        this.add(q1, getCoords(CCType.newQ));

        q1_multiple = new JRadioButton("Yes");
        q1_multiple.setSelected(false);
        q1_multiple.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q1_multiple.isSelected()) {
                    q1_one.setSelected(!q1_multiple.isSelected());
                } else {
                    q1_multiple.setSelected(true);
                }
            }
        });
        this.add(q1_multiple, getCoords(CCType.QToYes));
        
        q1_one = new JRadioButton("No");
        q1_one.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q1_one.isSelected()) {
                    q1_multiple.setSelected(!q1_one.isSelected());
                } else {
                    q1_one.setSelected(true);
                }
            }
        });
        q1_one.setSelected(true);
        this.add(q1_one, getCoords(CCType.YesToNo));
        
        /*
         * 
         */
        q5 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Is the fleet limited (i.e. limited number of vehicles of each type)?");
        this.add(q5, getCoords(CCType.newQ));

        q5_yes = new JRadioButton("Yes");
        q5_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q5_yes.isSelected()) {
                    q5_no.setSelected(!q5_yes.isSelected());
                } else {
                    q5_yes.setSelected(true);
                }
            }
        });
        q5_yes.setSelected(true);
        this.add(q5_yes, getCoords(CCType.QToYes));

        q5_no = new JRadioButton("No");
        q5_no.setSelected(false);
        q5_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q5_no.isSelected()) {
                    q5_yes.setSelected(!q5_no.isSelected());
                } else {
                    q5_no.setSelected(true);
                }
            }
        });
        this.add(q5_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q6 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Are there any accessibility issues (i.e. some vehicles cannot reach certain nodes)?");
        this.add(q6, getCoords(CCType.newQ));

        q6_yes = new JRadioButton("Yes");
        q6_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q6_yes.isSelected()) {
                    q6_no.setSelected(!q6_yes.isSelected());
                } else {
                    q6_yes.setSelected(true);
                }
            }
        });
        this.add(q6_yes, getCoords(CCType.QToYes));

        q6_no = new JRadioButton("No");
        q6_no.setSelected(false);
        q6_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q6_no.isSelected()) {
                    q6_yes.setSelected(!q6_no.isSelected());
                } else {
                    q6_no.setSelected(true);
                }
            }
        });
        q6_no.setSelected(true);
        this.add(q6_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q30 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Does the departure node of each vehicle need to be specified?");
        this.add(q30, getCoords(CCType.newQ));

        q30_yes = new JRadioButton("Yes");
        q30_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q30_yes.isSelected()) {
                    q30_no.setSelected(!q30_yes.isSelected());
                } else {
                    q30_yes.setSelected(true);
                }
            }
        });
        this.add(q30_yes, getCoords(CCType.QToYes));

        q30_no = new JRadioButton("No");
        q30_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q30_no.isSelected()) {
                    q30_yes.setSelected(!q30_no.isSelected());
                } else {
                    q30_no.setSelected(true);
                }
            }
        });
        q30_no.setSelected(true);
        this.add(q30_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q31 = new JLabel("<html><br />"+String.valueOf(qNum++)+". Does the arrival node of each vehicle need to be specified?");
        this.add(q31, getCoords(CCType.newQ));

        q31_yes = new JRadioButton("Yes");
        q31_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q31_yes.isSelected()) {
                    q31_no.setSelected(!q31_yes.isSelected());
                } else {
                    q31_yes.setSelected(true);
                }
            }
        });
        this.add(q31_yes, getCoords(CCType.QToYes));

        q31_no = new JRadioButton("No");
        q31_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q31_no.isSelected()) {
                    q31_yes.setSelected(!q31_no.isSelected());
                } else {
                    q31_no.setSelected(true);
                }
            }
        });
        q31_no.setSelected(true);
        this.add(q31_no, getCoords(CCType.YesToNo));
        /*
         * 
         * 
         */
        lbl10 = new JLabel("<html><br />Vehicle Characteristics :<br />");
        lbl10.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        this.add(lbl10, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q3 = new JLabel("<html><br />"+String.valueOf(qNum++)+". Do the vehicles have fixed costs?");
        this.add(q3, getCoords(CCType.newQ));

        q3_yes = new JRadioButton("Yes");
        q3_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q3_yes.isSelected()) {
                    q3_no.setSelected(!q3_yes.isSelected());
                } else {
                    q3_yes.setSelected(true);
                }
            }
        });
        this.add(q3_yes, getCoords(CCType.QToYes));

        q3_no = new JRadioButton("No");
        q3_no.setSelected(false);
        q3_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q3_no.isSelected()) {
                    q3_yes.setSelected(!q3_no.isSelected());
                } else {
                    q3_no.setSelected(true);
                }
            }
        });
        q3_no.setSelected(true);
        this.add(q3_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q4 = new JLabel("<html><br />"+String.valueOf(qNum++)+". Do the vehicles have variable costs?");
        this.add(q4, getCoords(CCType.newQ));

        q4_yes = new JRadioButton("Yes");
        q4_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q4_yes.isSelected()) {
                    q4_no.setSelected(!q4_yes.isSelected());
                } else {
                    q4_yes.setSelected(true);
                }
            }
        });
        this.add(q4_yes, getCoords(CCType.QToYes));

        q4_no = new JRadioButton("No");
        q4_no.setSelected(false);
        q4_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q4_no.isSelected()) {
                    q4_yes.setSelected(!q4_no.isSelected());
                } else {
                    q4_no.setSelected(true);
                }
            }
        });
        q4_no.setSelected(true);
        this.add(q4_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q14 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Do the vehicles have distance constraints (i.e. Are they vehicles limited to a certain travelling distance)?");
        this.add(q14, getCoords(CCType.newQ));

        q14_yes = new JRadioButton("Yes");
        q14_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q14_yes.isSelected()) {
                    q14_no.setSelected(!q14_yes.isSelected());
                    q15.setVisible(true);
                    q15_yes.setVisible(true);
                    q15_no.setVisible(true);
                } else {
                    q14_yes.setSelected(true);
                }
            }
        });
        this.add(q14_yes, getCoords(CCType.QToYes));

        q14_no = new JRadioButton("No");
        q14_no.setSelected(false);
        q14_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q14_no.isSelected()) {
                    q14_yes.setSelected(!q14_no.isSelected());
                    q15.setVisible(false);
                    q15_yes.setVisible(false);
                    q15_no.setVisible(false);
                } else {
                    q14_no.setSelected(true);
                }
            }
        });
        q14_no.setSelected(true);
        this.add(q14_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q15 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Is the distance constraint soft or hard?");
        q15.setVisible(false);
        this.add(q15, getCoords(CCType.newQ));

        q15_yes = new JRadioButton("Hard");
        q15_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q15_yes.isSelected()) {
                    q15_no.setSelected(!q15_yes.isSelected());
                } else {
                    q15_yes.setSelected(true);
                }
            }
        });
        q15_yes.setVisible(false);
        q15_yes.setSelected(true);
        this.add(q15_yes, getCoords(CCType.QToYes));

        q15_no = new JRadioButton("Soft");
        q15_no.setSelected(false);
        q15_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q15_no.isSelected()) {
                    q15_yes.setSelected(!q15_no.isSelected());
                } else {
                    q15_no.setSelected(true);
                }
            }
        });
        q15_no.setVisible(false);
        this.add(q15_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q16 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Is there a constraint on the maximum number of requests that a vehicle can service?");
        this.add(q16, getCoords(CCType.newQ));

        q16_yes = new JRadioButton("Yes");
        q16_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q16_yes.isSelected()) {
                    q16_no.setSelected(!q16_yes.isSelected());
                    q17.setVisible(true);
                    q17_yes.setVisible(true);
                    q17_no.setVisible(true);
                } else {
                    q16_yes.setSelected(true);
                }
            }
        });
        this.add(q16_yes, getCoords(CCType.QToYes));

        q16_no = new JRadioButton("No");
        q16_no.setSelected(false);
        q16_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q16_no.isSelected()) {
                    q16_yes.setSelected(!q16_no.isSelected());
                    q17.setVisible(false);
                    q17_yes.setVisible(false);
                    q17_no.setVisible(false);
                } else {
                    q16_no.setSelected(true);
                }
            }
        });
        q16_no.setSelected(true);
        this.add(q16_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q17 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Is the maximum number of requests soft or hard?");
        q17.setVisible(false);
        this.add(q17, getCoords(CCType.newQ));

        q17_yes = new JRadioButton("Hard");
        q17_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q17_yes.isSelected()) {
                    q17_no.setSelected(!q17_yes.isSelected());
                } else {
                    q17_yes.setSelected(true);
                }
            }
        });
        q17_yes.setVisible(false);
        q17_yes.setSelected(true);
        this.add(q17_yes, getCoords(CCType.QToYes));

        q17_no = new JRadioButton("Soft");
        q17_no.setSelected(false);
        q17_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q17_no.isSelected()) {
                    q17_yes.setSelected(!q17_no.isSelected());
                } else {
                    q17_no.setSelected(true);
                }
            }
        });
        q17_no.setVisible(false);
        this.add(q17_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q32 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Do the vehicles have a particular set of skills (e.g. technicians have different qualifications)?");
        this.add(q32, getCoords(CCType.newQ));

        q32_yes = new JRadioButton("Yes");
        q32_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q32_yes.isSelected()) {
                    q32_no.setSelected(!q32_yes.isSelected());
                } else {
                    q32_yes.setSelected(true);
                }
            }
        });
        this.add(q32_yes, getCoords(CCType.QToYes));

        q32_no = new JRadioButton("No");
        q32_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q32_no.isSelected()) {
                    q32_yes.setSelected(!q32_no.isSelected());
                } else {
                    q32_no.setSelected(true);
                }
            }
        });
        q32_no.setSelected(true);
        this.add(q32_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q33 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Do the vehicles carry a particular set of tools (e.g. tools for a specific task)?");
        this.add(q33, getCoords(CCType.newQ));

        q33_yes = new JRadioButton("Yes");
        q33_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q33_yes.isSelected()) {
                    q33_no.setSelected(!q33_yes.isSelected());
                } else {
                    q33_yes.setSelected(true);
                }
            }
        });
        this.add(q33_yes, getCoords(CCType.QToYes));

        q33_no = new JRadioButton("No");
        q33_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q33_no.isSelected()) {
                    q33_yes.setSelected(!q33_no.isSelected());
                } else {
                    q33_no.setSelected(true);
                }
            }
        });
        q33_no.setSelected(true);
        this.add(q33_no, getCoords(CCType.YesToNo));
        /*
         * 
         * 
         */
        lbl2 = new JLabel("<html><br /><br />Vehicle Speed :<br />");
        lbl2.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        this.add(lbl2, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q7 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Do the vehicles require speed profiles to be defined (e.g., average vehicle speed, time dependent travel times, ...)?");
        this.add(q7, getCoords(CCType.newQ));

        q7_yes = new JRadioButton("Yes");
        q7_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q7_yes.isSelected()) {
                    q7_no.setSelected(!q7_yes.isSelected());
                    q8.setVisible(true);
                    q8_average.setVisible(true);
                    q8_intervals.setVisible(true);
                    q8_average.setSelected(true);
                    q8_intervals.setSelected(false);
                } else {
                    q7_yes.setSelected(true);
                }
            }
        });
        this.add(q7_yes, getCoords(CCType.QToYes));

        q7_no = new JRadioButton("No");
        q7_no.setSelected(false);
        q7_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q7_no.isSelected()) {
                    q7_yes.setSelected(!q7_no.isSelected());
                    q8.setVisible(false);
                    q8_average.setVisible(false);
                    q8_intervals.setVisible(false);
                    q9.setVisible(false);
                    q9_yes.setVisible(false);
                    q9_no.setVisible(false);
                    q10.setVisible(false);
                    q10_yes.setVisible(false);
                    q10_no.setVisible(false);
                    q11.setVisible(false);
                    q11_start.setVisible(false);
                    q11_end.setVisible(false);
                    q8_average.setSelected(false);
                    q8_intervals.setSelected(false);
                } else {
                    q7_no.setSelected(true);
                }
            }
        });
        q7_no.setSelected(true);
        this.add(q7_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q8 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". What types of speed characteristics need to be implemented?");
        q8.setVisible(false);
        this.add(q8, getCoords(CCType.newQ));

        q8_average = new JCheckBox("Average speed");
        q8_average.setVisible(false);
        q8_average.setSelected(true);
        this.add(q8_average, getCoords(CCType.QToOption));

        q8_intervals = new JCheckBox(
                "Different speeds during different time windows (e.g. 30mph from 5-7p.m.)");
        q8_intervals.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q8_intervals.isSelected()) {
                    q9.setVisible(true);
                    q9_yes.setVisible(true);
                    q9_no.setVisible(true);
                    q10.setVisible(true);
                    q10_yes.setVisible(true);
                    q10_no.setVisible(true);
                    q11.setVisible(true);
                    q11_start.setVisible(true);
                    q11_end.setVisible(true);
                    q12.setVisible(true);
                    q12_yes.setVisible(true);
                    q12_no.setVisible(true);
                    q13.setVisible(true);
                    q13_yes.setVisible(true);
                    q13_no.setVisible(true);
                } else {
                    q9.setVisible(false);
                    q9_yes.setVisible(false);
                    q9_no.setVisible(false);
                    q10.setVisible(false);
                    q10_yes.setVisible(false);
                    q10_no.setVisible(false);
                    q11.setVisible(false);
                    q11_start.setVisible(false);
                    q11_end.setVisible(false);
                    q12.setVisible(false);
                    q12_yes.setVisible(false);
                    q12_no.setVisible(false);
                    q13.setVisible(false);
                    q13_yes.setVisible(false);
                    q13_no.setVisible(false);
                }
            }
        });
        q8_intervals.setVisible(false);
        this.add(q8_intervals, getCoords(CCType.OptionToOption));
        /*
         * 
         */
        q9 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Could a vehicle have multiple speed intervals (i.e. several different speeds)?");
        q9.setVisible(false);
        this.add(q9, getCoords(CCType.newQ));

        q9_yes = new JRadioButton("Yes");
        q9_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q9_yes.isSelected()) {
                    q9_no.setSelected(!q9_yes.isSelected());
                } else {
                    q9_yes.setSelected(true);
                }
            }
        });
        q9_yes.setVisible(false);
        this.add(q9_yes, getCoords(CCType.QToYes));

        q9_no = new JRadioButton("No");
        q9_no.setSelected(false);
        q9_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q9_no.isSelected()) {
                    q9_yes.setSelected(!q9_no.isSelected());
                } else {
                    q9_no.setSelected(true);
                }
            }
        });
        q9_no.setSelected(true);
        q9_no.setVisible(false);
        this.add(q9_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q10 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". For one speed interval, are several time windows used (e.g. 30mph from 9-10am and 5-7pm)?");
        q10.setVisible(false);
        this.add(q10, getCoords(CCType.newQ));

        q10_yes = new JRadioButton("Yes");
        q10_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q10_yes.isSelected()) {
                    q10_no.setSelected(!q10_yes.isSelected());
                } else {
                    q10_yes.setSelected(true);
                }
            }
        });
        q10_yes.setVisible(false);
        this.add(q10_yes, getCoords(CCType.QToYes));

        q10_no = new JRadioButton("No");
        q10_no.setSelected(false);
        q10_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q10_no.isSelected()) {
                    q10_yes.setSelected(!q10_no.isSelected());
                } else {
                    q10_no.setSelected(true);
                }
            }
        });
        q10_no.setSelected(true);
        q10_no.setVisible(false);
        this.add(q10_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q11 = new JLabel("<html><br />"+String.valueOf(qNum++)+". What values do the time windows have?");
        q11.setVisible(false);
        this.add(q11, getCoords(CCType.newQ));

        q11_start = new JCheckBox("Start");
        q11_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q11_start.isSelected()) {
                    q12.setVisible(true);
                    q12_yes.setVisible(true);
                    q12_no.setVisible(true);
                } else {
                    q12.setVisible(false);
                    q12_yes.setVisible(false);
                    q12_no.setVisible(false);
                }
            }
        });
        q11_start.setVisible(false);
        q11_start.setSelected(true);
        this.add(q11_start, getCoords(CCType.QToOption));

        q11_end = new JCheckBox("End");
        q11_end.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q11_end.isSelected()) {
                    q13.setVisible(true);
                    q13_yes.setVisible(true);
                    q13_no.setVisible(true);
                } else {
                    q13.setVisible(false);
                    q13_yes.setVisible(false);
                    q13_no.setVisible(false);
                }
            }
        });
        q11_end.setVisible(false);
        q11_end.setSelected(true);
        this.add(q11_end, getCoords(CCType.OptionToOption));
        /*
         * 
         */
        q12 = new JLabel("<html><br />"+String.valueOf(qNum++)+". Is the start time strict (i.e. is not variable)?");
        q12.setVisible(false);
        this.add(q12, getCoords(CCType.newQ));

        q12_yes = new JRadioButton("Yes");
        q12_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q12_yes.isSelected()) {
                    q12_no.setSelected(!q12_yes.isSelected());
                } else {
                    q12_yes.setSelected(true);
                }
            }
        });
        q12_yes.setVisible(false);
        q12_yes.setSelected(true);
        this.add(q12_yes, getCoords(CCType.QToYes));

        q12_no = new JRadioButton("No");
        q12_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q12_no.isSelected()) {
                    q12_yes.setSelected(!q12_no.isSelected());
                } else {
                    q12_no.setSelected(true);
                }
            }
        });
        q12_no.setVisible(false);
        this.add(q12_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q13 = new JLabel("<html><br />"+String.valueOf(qNum++)+". Is the end time strict (i.e. is not variable)?");
        q13.setVisible(false);
        this.add(q13, getCoords(CCType.newQ));

        q13_yes = new JRadioButton("Yes");
        q13_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q13_yes.isSelected()) {
                    q13_no.setSelected(!q13_yes.isSelected());
                } else {
                    q13_yes.setSelected(true);
                }
            }
        });
        q13_yes.setVisible(false);
        q13_yes.setSelected(true);
        this.add(q13_yes, getCoords(CCType.QToYes));

        q13_no = new JRadioButton("No");
        q13_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q13_no.isSelected()) {
                    q13_yes.setSelected(!q13_no.isSelected());
                } else {
                    q13_no.setSelected(true);
                }
            }
        });
        q13_no.setVisible(false);
        this.add(q13_no, getCoords(CCType.YesToNo));
        /*
         * 
         * 
         */
        lbl3 = new JLabel("<html><br /><br />Workload Profile :<br />");
        lbl3.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        this.add(lbl3, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q18 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Do the vehicles/drivers have work time restrictions?");
        this.add(q18, getCoords(CCType.newQ));

        q18_yes = new JRadioButton("Yes");
        q18_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q18_yes.isSelected()) {
                    q18_no.setSelected(!q18_yes.isSelected());
                    q19.setVisible(true);
                    q19_yes.setVisible(true);
                    q19_no.setVisible(true);
                    q21.setVisible(true);
                    q21_yes.setVisible(true);
                    q21_no.setVisible(true);
                    q101.setVisible(true);
                    q101_yes.setVisible(true);
                    q101_no.setVisible(true);
                } else {
                    q18_yes.setSelected(true);
                }
            }
        });
        this.add(q18_yes, getCoords(CCType.QToYes));

        q18_no = new JRadioButton("No");
        q18_no.setSelected(false);
        q18_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q18_no.isSelected()) {
                    q18_yes.setSelected(!q18_no.isSelected());
                    q19.setVisible(false);
                    q19_yes.setVisible(false);
                    q19_no.setVisible(false);
                    q20.setVisible(false);
                    q20_yes.setVisible(false);
                    q20_no.setVisible(false);
                    q21.setVisible(false);
                    q21_yes.setVisible(false);
                    q21_no.setVisible(false);
                    q101.setVisible(false);
                    q21_yes.setSelected(false);
                    q21_no.setSelected(true);
                    q101_yes.setVisible(false);
                    q101_no.setVisible(false);
                    q22.setVisible(false);
                    q22_yes.setVisible(false);
                    q22_no.setVisible(false);
                    q23.setVisible(false);
                    q23_start.setVisible(false);
                    q23_end.setVisible(false);
                    q24.setVisible(false);
                    q24_yes.setVisible(false);
                    q24_no.setVisible(false);
                    q25.setVisible(false);
                    q25_yes.setVisible(false);
                    q25_no.setVisible(false);
                    q101_yes.setSelected(false);
                    q101_no.setSelected(true);
                    lblWLPAny.setVisible(false);
                    wLPAny.setVisible(false);
                    wLPAny.getListModel().clear();
                } else {
                    q18_no.setSelected(true);
                }
            }
        });
        q18_no.setSelected(true);
        this.add(q18_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q19 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Do the vehicles have a maximum work time (e.g. 9 hours per day)?");
        q19.setVisible(false);
        this.add(q19, getCoords(CCType.newQ));

        q19_yes = new JRadioButton("Yes");
        q19_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q19_yes.isSelected()) {
                    q19_no.setSelected(!q19_yes.isSelected());
                    q20.setVisible(true);
                    q20_yes.setVisible(true);
                    q20_no.setVisible(true);
                } else {
                    q19_yes.setSelected(true);
                }
            }
        });
        q19_yes.setVisible(false);
        q19_yes.setSelected(true);
        this.add(q19_yes, getCoords(CCType.QToYes));

        q19_no = new JRadioButton("No");
        q19_no.setSelected(false);
        q19_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q19_no.isSelected()) {
                    q19_yes.setSelected(!q19_no.isSelected());
                    q20.setVisible(false);
                    q20_yes.setVisible(false);
                    q20_no.setVisible(false);
                } else {
                    q19_no.setSelected(true);
                }
            }
        });
        q19_no.setVisible(false);
        this.add(q19_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q20 = new JLabel("<html><br />"+String.valueOf(qNum++)+". Is the maximum work time soft or hard?");
        q20.setVisible(false);
        this.add(q20, getCoords(CCType.newQ));

        q20_yes = new JRadioButton("Hard");
        q20_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q20_yes.isSelected()) {
                    q20_no.setSelected(!q20_yes.isSelected());
                } else {
                    q20_yes.setSelected(true);
                }
            }
        });
        q20_yes.setVisible(false);
        q20_yes.setSelected(true);
        this.add(q20_yes, getCoords(CCType.QToYes));

        q20_no = new JRadioButton("Soft");
        q20_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q20_no.isSelected()) {
                    q20_yes.setSelected(!q20_no.isSelected());
                } else {
                    q20_no.setSelected(true);
                }
            }
        });
        q20_no.setVisible(false);
        this.add(q20_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q21 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Are the vehicles only able to operate during certain periods/time windows (e.g.  from 9am to 5pm)?");
        q21.setVisible(false);
        this.add(q21, getCoords(CCType.newQ));

        q21_yes = new JRadioButton("Yes");
        q21_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q21_yes.isSelected()) {
                    q21_no.setSelected(!q21_yes.isSelected());
                    q22.setVisible(true);
                    q22_yes.setVisible(true);
                    q22_no.setVisible(true);
                    q23.setVisible(true);
                    q23_start.setVisible(true);
                    q23_end.setVisible(true);
                    q24.setVisible(true);
                    q24_yes.setVisible(true);
                    q24_no.setVisible(true);
                    q25.setVisible(true);
                    q25_yes.setVisible(true);
                    q25_no.setVisible(true);
                } else {
                    q21_yes.setSelected(true);
                }
            }
        });
        q21_yes.setVisible(false);
        this.add(q21_yes, getCoords(CCType.QToYes));

        q21_no = new JRadioButton("No");
        q21_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q21_no.isSelected()) {

                    q21_yes.setSelected(!q21_no.isSelected());
                    q22.setVisible(false);
                    q22_yes.setVisible(false);
                    q22_no.setVisible(false);
                    q23.setVisible(false);
                    q23_start.setVisible(false);
                    q23_end.setVisible(false);
                    q24.setVisible(false);
                    q24_yes.setVisible(false);
                    q24_no.setVisible(false);
                    q25.setVisible(false);
                    q25_yes.setVisible(false);
                    q25_no.setVisible(false);
                } else {
                    q21_no.setSelected(true);
                }
            }
        });
        q21_no.setSelected(true);
        q21_no.setVisible(false);
        this.add(q21_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q22 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Could a vehicle be restricted to several different periods (e.g. from 9-10am AND from 4-5pm)?");
        q22.setVisible(false);
        this.add(q22, getCoords(CCType.newQ));

        q22_yes = new JRadioButton("Yes");
        q22_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q22_yes.isSelected()) {
                    q22_no.setSelected(!q22_yes.isSelected());
                } else {
                    q22_yes.setSelected(true);
                }
            }
        });
        q22_yes.setVisible(false);
        this.add(q22_yes, getCoords(CCType.QToYes));

        q22_no = new JRadioButton("No");
        q22_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q22_no.isSelected()) {
                    q22_yes.setSelected(!q22_no.isSelected());
                } else {
                    q22_no.setSelected(true);
                }
            }
        });
        q22_no.setSelected(true);
        q22_no.setVisible(false);
        this.add(q22_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q23 = new JLabel("<html><br />"+String.valueOf(qNum++)+". What values do the time windows have?");
        q23.setVisible(false);
        this.add(q23, getCoords(CCType.newQ));

        q23_start = new JCheckBox("Start");
        q23_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q23_start.isSelected()) {
                    q24.setVisible(true);
                    q24_yes.setVisible(true);
                    q24_no.setVisible(true);
                } else {
                    q24.setVisible(false);
                    q24_yes.setVisible(false);
                    q24_no.setVisible(false);
                }
            }
        });
        q23_start.setSelected(true);
        q23_start.setVisible(false);
        this.add(q23_start, getCoords(CCType.QToYes));

        q23_end = new JCheckBox("End");
        q23_end.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q23_end.isSelected()) {
                    q25.setVisible(true);
                    q25_yes.setVisible(true);
                    q25_no.setVisible(true);
                } else {
                    q25.setVisible(false);
                    q25_yes.setVisible(false);
                    q25_no.setVisible(false);
                }
            }
        });
        q23_end.setSelected(true);
        q23_end.setVisible(false);
        this.add(q23_end, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q24 = new JLabel("<html><br />"+String.valueOf(qNum++)+". Is the start time strict (i.e. is not variable)?");
        q24.setVisible(false);
        this.add(q24, getCoords(CCType.newQ));

        q24_yes = new JRadioButton("Yes");
        q24_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q24_yes.isSelected()) {
                    q24_no.setSelected(!q24_yes.isSelected());
                } else {
                    q24_yes.setSelected(true);
                }
            }
        });
        q24_yes.setSelected(true);
        q24_yes.setVisible(false);
        this.add(q24_yes, getCoords(CCType.QToYes));

        q24_no = new JRadioButton("No");
        q24_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q24_no.isSelected()) {
                    q24_yes.setSelected(!q24_no.isSelected());
                } else {
                    q24_no.setSelected(true);
                }
            }
        });
        q24_no.setVisible(false);
        this.add(q24_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q25 = new JLabel("<html><br />"+String.valueOf(qNum++)+". Is the end time strict (i.e. is not variable)?");
        q25.setVisible(false);
        this.add(q25, getCoords(CCType.newQ));

        q25_yes = new JRadioButton("Yes");
        q25_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q25_yes.isSelected()) {
                    q25_no.setSelected(!q25_yes.isSelected());
                } else {
                    q25_yes.setSelected(true);
                }
            }
        });
        q25_yes.setSelected(true);
        q25_yes.setVisible(false);
        this.add(q25_yes, getCoords(CCType.QToYes));

        q25_no = new JRadioButton("No");
        q25_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q25_no.isSelected()) {
                    q25_yes.setSelected(!q25_no.isSelected());
                } else {
                    q25_no.setSelected(true);
                }
            }
        });
        q25_no.setVisible(false);
        this.add(q25_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q101 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Are there any other properties that need to be added to the workload profile?");
        q101.setVisible(false);
        this.add(q101, getCoords(CCType.newQ));

        q101_yes = new JRadioButton("Yes");
        q101_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q101_yes.isSelected()) {
                    q101_no.setSelected(!q101_yes.isSelected());
                    lblWLPAny.setVisible(true);
                    wLPAny.setVisible(true);
                } else {
                    q101_yes.setSelected(true);
                }
            }
        });
        q101_yes.setVisible(false);
        this.add(q101_yes, getCoords(CCType.QToYes));

        q101_no = new JRadioButton("No");
        q101_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q101_no.isSelected()) {
                    q101_yes.setSelected(!q101_no.isSelected());
                    lblWLPAny.setVisible(false);
                    wLPAny.setVisible(false);
                } else {
                    q101_no.setSelected(true);
                }
            }
        });
        q101_no.setSelected(true);
        q101_no.setVisible(false);
        this.add(q101_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        lblWLPAny.setVisible(false);
        this.add(lblWLPAny, getCoords(CCType.newQ));
        /*
         * 
         */
        wLPAny = new AnyList();
        wLPAny.setVisible(false);
        this.add(wLPAny, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        lbl4 = new JLabel("<html><br /><br />Capacities :<br />");
        lbl4.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        this.add(lbl4, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q26 = new JLabel("<html><br />"+String.valueOf(qNum++)+". Do the vehicles have capacity constraints?");
        this.add(q26, getCoords(CCType.newQ));

        q26_yes = new JRadioButton("Yes");
        q26_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q26_yes.isSelected()) {
                    q26_no.setSelected(!q26_yes.isSelected());
                    q28.setVisible(true);
                    q28_yes.setVisible(true);
                    q28_no.setVisible(true);
                    q29.setVisible(false);
                    q29_minMax.setVisible(false);
                    q29_fixed.setVisible(false);
                    q28_yes.setSelected(false);
                    q28_no.setSelected(true);
                } else {
                    q26_yes.setSelected(true);
                }
            }
        });
        this.add(q26_yes, getCoords(CCType.QToYes));

        q26_no = new JRadioButton("No");
        q26_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q26_no.isSelected()) {
                    q26_yes.setSelected(!q26_no.isSelected());
                    q28.setVisible(false);
                    q28_yes.setVisible(false);
                    q28_no.setVisible(false);
                    q29.setVisible(false);
                    q29_minMax.setVisible(false);
                    q29_fixed.setVisible(false);
                    q28_yes.setSelected(true);
                    q28_no.setSelected(false);
                    q29.setVisible(false);
                    q29_minMax.setVisible(false);
                    q29_fixed.setVisible(false);
                    q29_minMax.setSelected(false);
                    q29_fixed.setSelected(false);
                } else {
                    q26_no.setSelected(true);
                }
            }
        });
        q26_no.setSelected(true);
        this.add(q26_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q28 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Do the vehicles have several different capacities (e.g. multiple compartments)?");
        q28.setVisible(false);
        this.add(q28, getCoords(CCType.newQ));

        q28_yes = new JRadioButton("Yes");
        q28_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q28_yes.isSelected()) {
                    q28_no.setSelected(!q28_yes.isSelected());
                    q29.setVisible(true);
                    q29_minMax.setVisible(true);
                    q29_fixed.setVisible(true);
                    q29_minMax.setSelected(false);
                    q29_fixed.setSelected(true);
                } else {
                    q28_yes.setSelected(true);
                }
            }
        });
        q28_yes.setVisible(false);
        this.add(q28_yes, getCoords(CCType.QToYes));

        q28_no = new JRadioButton("No");
        q28_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q28_no.isSelected()) {
                    q28_yes.setSelected(!q28_no.isSelected());
                    q29.setVisible(false);
                    q29_minMax.setVisible(false);
                    q29_fixed.setVisible(false);
                    q29_minMax.setSelected(false);
                    q29_fixed.setSelected(false);
                } else {
                    q28_no.setSelected(true);
                }
            }
        });
        q28_no.setSelected(true);
        q28_no.setVisible(false);
        this.add(q28_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q29 = new JLabel("<html><br />"+String.valueOf(qNum++)+". What kind of capacities need to be defined?");
        q29.setVisible(false);
        this.add(q29, getCoords(CCType.newQ));

        q29_minMax = new JCheckBox("Minimum and maximum compartment capacity");
        q29_minMax.setVisible(false);
        this.add(q29_minMax, getCoords(CCType.QToOption));

        q29_fixed = new JCheckBox("Fixed compartment capacity");
        q29_fixed.setVisible(false);
        this.add(q29_fixed, getCoords(CCType.QToOption));
        /*
         * 
         * 
         */
        lbl11 = new JLabel("<html><br />Extra Vehicle Constraints :<br />");
        lbl11.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        this.add(lbl11, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q105 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Do the vehicles require any more constraints to be properly represented?");
        this.add(q105, getCoords(CCType.newQ));

        q105_yes = new JRadioButton("Yes");
        q105_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q105_yes.isSelected()) {
                    q105_no.setSelected(!q105_yes.isSelected());
                    lblAnyMore.setVisible(true);
                    anyMoreAny.setVisible(true);
                } else {
                    q105_yes.setSelected(true);
                }
            }
        });
        this.add(q105_yes, getCoords(CCType.QToYes));

        q105_no = new JRadioButton("No");
        q105_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q105_no.isSelected()) {
                    q105_yes.setSelected(!q105_no.isSelected());
                    lblAnyMore.setVisible(false);
                    anyMoreAny.setVisible(false);
                } else {
                    q105_no.setSelected(true);
                }
            }
        });
        q105_no.setSelected(true);
        this.add(q105_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        lblAnyMore.setVisible(false);
        lblAnyMore.setVisible(false);
        this.add(lblAnyMore, getCoords(CCType.newQ));
        /*
         * 
         */
        anyMoreAny = new AnyList();
        anyMoreAny.setVisible(false);
        this.add(anyMoreAny, getCoords(CCType.newQ));
        /*
         * 
         * 
         * 
         */
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(50, 50));
        this.add(spacer, getCoords(CCType.newQ));

    }

    /**
     * Return boolean values of speed types
     * 
     * @return table containing values
     */
    public boolean[] getSpeedTypes() {
        boolean[] values = { q8_average.isSelected(), q8_intervals.isSelected() };
        return values;
    }

    /**
     * Return boolean values of cpacity types
     * 
     * @return table containing values
     */
    public boolean[] getCapacityTypes() {
        boolean[] values = { q29_minMax.isSelected(), q29_fixed.isSelected() };
        return values;
    }

    /**
     * Getter for the variable q7_yes
     * 
     * @return the q7_yes
     */
    public JRadioButton getQ7_yes() {
        return q7_yes;
    }

    /**
     * Getter for the variable q26_yes
     * 
     * @return the q26_yes
     */
    public JRadioButton getQ26_yes() {
        return q26_yes;
    }

    /**
     * Getter for the variable q1_one
     * 
     * @return the q1_one
     */
    public JRadioButton getQ1_one() {
        return q1_one;
    }

    /**
     * Getter for the variable q1_multiple
     * 
     * @return the q1_multiple
     */
    public JRadioButton getQ1_multiple() {
        return q1_multiple;
    }

    /**
     * Getter for the variable q3_yes
     * 
     * @return the q3_yes
     */
    public JRadioButton getQ3_yes() {
        return q3_yes;
    }

    /**
     * Getter for the variable q3_no
     * 
     * @return the q3_no
     */
    public JRadioButton getQ3_no() {
        return q3_no;
    }

    /**
     * Getter for the variable q4_yes
     * 
     * @return the q4_yes
     */
    public JRadioButton getQ4_yes() {
        return q4_yes;
    }

    /**
     * Getter for the variable q4_no
     * 
     * @return the q4_no
     */
    public JRadioButton getQ4_no() {
        return q4_no;
    }

    /**
     * Getter for the variable q5_yes
     * 
     * @return the q5_yes
     */
    public JRadioButton getQ5_yes() {
        return q5_yes;
    }

    /**
     * Getter for the variable q5_no
     * 
     * @return the q5_no
     */
    public JRadioButton getQ5_no() {
        return q5_no;
    }

    /**
     * Getter for the variable q6_yes
     * 
     * @return the q6_yes
     */
    public JRadioButton getQ6_yes() {
        return q6_yes;
    }

    /**
     * Getter for the variable q6_no
     * 
     * @return the q6_no
     */
    public JRadioButton getQ6_no() {
        return q6_no;
    }

    /**
     * Getter for the variable q7_no
     * 
     * @return the q7_no
     */
    public JRadioButton getQ7_no() {
        return q7_no;
    }

    /**
     * Getter for the variable q8_average
     * 
     * @return the q8_average
     */
    public JCheckBox getQ8_average() {
        return q8_average;
    }

    /**
     * Getter for the variable q8_intervals
     * 
     * @return the q8_intervals
     */
    public JCheckBox getQ8_intervals() {
        return q8_intervals;
    }

    /**
     * Getter for the variable q9_yes
     * 
     * @return the q9_yes
     */
    public JRadioButton getQ9_yes() {
        return q9_yes;
    }

    /**
     * Getter for the variable q9_no
     * 
     * @return the q9_no
     */
    public JRadioButton getQ9_no() {
        return q9_no;
    }

    /**
     * Getter for the variable q10_yes
     * 
     * @return the q10_yes
     */
    public JRadioButton getQ10_yes() {
        return q10_yes;
    }

    /**
     * Getter for the variable q10_no
     * 
     * @return the q10_no
     */
    public JRadioButton getQ10_no() {
        return q10_no;
    }

    /**
     * Getter for the variable q11_start
     * 
     * @return the q11_start
     */
    public JCheckBox getQ11_start() {
        return q11_start;
    }

    /**
     * Getter for the variable q11_end
     * 
     * @return the q11_end
     */
    public JCheckBox getQ11_end() {
        return q11_end;
    }

    /**
     * Getter for the variable q12_yes
     * 
     * @return the q12_yes
     */
    public JRadioButton getQ12_yes() {
        return q12_yes;
    }

    /**
     * Getter for the variable q12_no
     * 
     * @return the q12_no
     */
    public JRadioButton getQ12_no() {
        return q12_no;
    }

    /**
     * Getter for the variable q13_yes
     * 
     * @return the q13_yes
     */
    public JRadioButton getQ13_yes() {
        return q13_yes;
    }

    /**
     * Getter for the variable q13_no
     * 
     * @return the q13_no
     */
    public JRadioButton getQ13_no() {
        return q13_no;
    }

    /**
     * Getter for the variable q14_yes
     * 
     * @return the q14_yes
     */
    public JRadioButton getQ14_yes() {
        return q14_yes;
    }

    /**
     * Getter for the variable q14_no
     * 
     * @return the q14_no
     */
    public JRadioButton getQ14_no() {
        return q14_no;
    }

    /**
     * Getter for the variable q15_yes
     * 
     * @return the q15_yes
     */
    public JRadioButton getQ15_yes() {
        return q15_yes;
    }

    /**
     * Getter for the variable q15_no
     * 
     * @return the q15_no
     */
    public JRadioButton getQ15_no() {
        return q15_no;
    }

    /**
     * Getter for the variable q16_yes
     * 
     * @return the q16_yes
     */
    public JRadioButton getQ16_yes() {
        return q16_yes;
    }

    /**
     * Getter for the variable q16_no
     * 
     * @return the q16_no
     */
    public JRadioButton getQ16_no() {
        return q16_no;
    }

    /**
     * Getter for the variable q17_yes
     * 
     * @return the q17_yes
     */
    public JRadioButton getQ17_yes() {
        return q17_yes;
    }

    /**
     * Getter for the variable q17_no
     * 
     * @return the q17_no
     */
    public JRadioButton getQ17_no() {
        return q17_no;
    }

    /**
     * Getter for the variable q18_yes
     * 
     * @return the q18_yes
     */
    public JRadioButton getQ18_yes() {
        return q18_yes;
    }

    /**
     * Getter for the variable q18_no
     * 
     * @return the q18_no
     */
    public JRadioButton getQ18_no() {
        return q18_no;
    }

    /**
     * Getter for the variable q19_yes
     * 
     * @return the q19_yes
     */
    public JRadioButton getQ19_yes() {
        return q19_yes;
    }

    /**
     * Getter for the variable q19_no
     * 
     * @return the q19_no
     */
    public JRadioButton getQ19_no() {
        return q19_no;
    }

    /**
     * Getter for the variable q20_yes
     * 
     * @return the q20_yes
     */
    public JRadioButton getQ20_yes() {
        return q20_yes;
    }

    /**
     * Getter for the variable q20_no
     * 
     * @return the q20_no
     */
    public JRadioButton getQ20_no() {
        return q20_no;
    }

    /**
     * Getter for the variable q21_yes
     * 
     * @return the q21_yes
     */
    public JRadioButton getQ21_yes() {
        return q21_yes;
    }

    /**
     * Getter for the variable q21_no
     * 
     * @return the q21_no
     */
    public JRadioButton getQ21_no() {
        return q21_no;
    }

    /**
     * Getter for the variable q22_yes
     * 
     * @return the q22_yes
     */
    public JRadioButton getQ22_yes() {
        return q22_yes;
    }

    /**
     * Getter for the variable q22_no
     * 
     * @return the q22_no
     */
    public JRadioButton getQ22_no() {
        return q22_no;
    }

    /**
     * Getter for the variable q23_start
     * 
     * @return the q23_start
     */
    public JCheckBox getQ23_start() {
        return q23_start;
    }

    /**
     * Getter for the variable q23_end
     * 
     * @return the q23_end
     */
    public JCheckBox getQ23_end() {
        return q23_end;
    }

    /**
     * Getter for the variable q24_yes
     * 
     * @return the q24_yes
     */
    public JRadioButton getQ24_yes() {
        return q24_yes;
    }

    /**
     * Getter for the variable q24_no
     * 
     * @return the q24_no
     */
    public JRadioButton getQ24_no() {
        return q24_no;
    }

    /**
     * Getter for the variable q25_yes
     * 
     * @return the q25_yes
     */
    public JRadioButton getQ25_yes() {
        return q25_yes;
    }

    /**
     * Getter for the variable q25_no
     * 
     * @return the q25_no
     */
    public JRadioButton getQ25_no() {
        return q25_no;
    }

    /**
     * Getter for the variable q26_no
     * 
     * @return the q26_no
     */
    public JRadioButton getQ26_no() {
        return q26_no;
    }

    /**
     * Getter for the variable q28_yes
     * 
     * @return the q28_yes
     */
    public JRadioButton getQ28_yes() {
        return q28_yes;
    }

    /**
     * Getter for the variable q28_no
     * 
     * @return the q28_no
     */
    public JRadioButton getQ28_no() {
        return q28_no;
    }

    /**
     * Getter for the variable q29_minMax
     * 
     * @return the q29_minMax
     */
    public JCheckBox getQ29_minMax() {
        return q29_minMax;
    }

    /**
     * Getter for the variable q29_fixed
     * 
     * @return the q29_fixed
     */
    public JCheckBox getQ29_fixed() {
        return q29_fixed;
    }

    /**
     * Getter for the variable q30_yes
     * 
     * @return the q30_yes
     */
    public JRadioButton getQ30_yes() {
        return q30_yes;
    }

    /**
     * Getter for the variable q30_no
     * 
     * @return the q30_no
     */
    public JRadioButton getQ30_no() {
        return q30_no;
    }

    /**
     * Getter for the variable q31_yes
     * 
     * @return the q31_yes
     */
    public JRadioButton getQ31_yes() {
        return q31_yes;
    }

    /**
     * Getter for the variable q31_no
     * 
     * @return the q31_no
     */
    public JRadioButton getQ31_no() {
        return q31_no;
    }

    /**
     * Getter for the variable q32_yes
     * 
     * @return the q32_yes
     */
    public JRadioButton getQ32_yes() {
        return q32_yes;
    }

    /**
     * Getter for the variable q32_no
     * 
     * @return the q32_no
     */
    public JRadioButton getQ32_no() {
        return q32_no;
    }

    /**
     * Getter for the variable q33_yes
     * 
     * @return the q33_yes
     */
    public JRadioButton getQ33_yes() {
        return q33_yes;
    }

    /**
     * Getter for the variable q33_no
     * 
     * @return the q33_no
     */
    public JRadioButton getQ33_no() {
        return q33_no;
    }

    /**
     * Getter for the variable q101_yes
     * 
     * @return the q101_yes
     */
    public JRadioButton getQ101_yes() {
        return q101_yes;
    }

    /**
     * Getter for the variable q101_no
     * 
     * @return the q101_no
     */
    public JRadioButton getQ101_no() {
        return q101_no;
    }

    /**
     * Getter for the variable wLPAny
     * 
     * @return the wLPAny
     */
    public AnyList getwLPAny() {
        return wLPAny;
    }
    
    

    /**
	 * Getter for the variable q105_yes
	 * @return the q105_yes
	 */
	public JRadioButton getQ105_yes() {
		return q105_yes;
	}

	/**
	 * Getter for the variable anyMoreAny
	 * @return the anyMoreAny
	 */
	public AnyList getAnyMoreAny() {
		return anyMoreAny;
	}

	/**
     * Create JAVA FormLayout coordinates for new component
     * 
     * @param cType
     *            type of component change (new questions, yes button, no button, etc...)
     * @return String value or coordinates
     */
    private String getCoords(CCType cType) {
        String result = "";
        switch (cType) {
        case newQ:
            xCoord = 2;
            yCoord += 2; // 4
            result = "" + xCoord + "," + yCoord + ",21,1";
            break;
        case QToYes:
            yCoord += 2;
            result = "" + xCoord + "," + yCoord + "";
            break;
        case YesToNo:
            xCoord = 4;
            result = "" + xCoord + "," + yCoord + "";
            break;
        case QToOption:
            xCoord = 4;
            yCoord += 1; // 2
            result = "" + xCoord + "," + yCoord + ",19,1";
            break;
        case OptionToOption:
            yCoord += 1; // 2
            result = "" + xCoord + "," + yCoord + ",19,1";
            break;
        }
        return result;
    }

    /**
     * Enumerator of different question/answer types possible
     * 
     * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
     */
    enum CCType {
        newQ, QToYes, YesToNo, QToOption, OptionToOption
    }

}
