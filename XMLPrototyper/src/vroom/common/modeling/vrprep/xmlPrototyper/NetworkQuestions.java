package vroom.common.modeling.vrprep.xmlPrototyper;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Contains all questions relevant to the network
 * 
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 */
public class NetworkQuestions extends JPanel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8850383537630813937L;

    private int               xCoord           = 2;
    private int               yCoord           = 4;

    private JLabel            q2;
    private JRadioButton      q2_nodeL;
    private JRadioButton      q2_linkL;
    private JRadioButton      q2_both;
    private JLabel            q4;
    private JCheckBox         q4_euclid;
    private JCheckBox         q4_gps;
    private JCheckBox         q4_other;
    private JLabel            lblLocationAny   = new JLabel(
                                                       "<html><br />Please enter the names of the elements that need to be created :");
    private AnyList           locationAny;
    private JLabel            q100;
    private JCheckBox         q100_cost;
    private JCheckBox         q100_length;
    private JCheckBox         q100_time;
    private JLabel            q7;
    private JRadioButton      q7_yes;
    private JRadioButton      q7_no;
    private JLabel            q8;
    private JRadioButton      q8_yes;
    private JRadioButton      q8_no;
    private JLabel            q12;
    private JCheckBox         q12_value;
    private JCheckBox         q12_normal;
    private JCheckBox         q12_poisson;
    private JCheckBox         q12_other;
    private JLabel            lblTimeAny       = new JLabel(
                                                       "<html><br />Please enter the names of the elements that need to be created :");
    private AnyList           timeAny;
    private JLabel            q13;
    private JRadioButton      q13_yes;
    private JRadioButton      q13_no;
    private JLabel            q14;
    private JRadioButton      q14_yes;
    private JRadioButton      q14_no;
    private JLabel            lblDistMesure;
    private JTextField        textFieldDistMesure;
    private JLabel            q15;
    private JRadioButton      q15_yes;
    private JRadioButton      q15_no;
    private JLabel            lblRoundingRule;
    private JTextField        textFieldRoundingRule;
    private JLabel            q16;
    private JRadioButton      q16_yes;
    private JRadioButton      q16_no;

    private JLabel            lblDescripAny    = new JLabel(
                                                       "<html><br />Please enter the names of the elements that need to be created :");
    private AnyList           descriptorAny;
    
    private JLabel			  q101;
    private JRadioButton	  q101_yes;
    private JRadioButton 	  q101_no;

    private JLabel            lbl1;
    private JLabel            lbl2;
    private JLabel            lbl3;
    private JLabel            lbl4;
    
    private JLabel            q105;
    private JRadioButton      q105_yes;
    private JRadioButton      q105_no;
    private JLabel            lblAnyMoreN        = new JLabel(
                                                       "<html><br />Please enter the names of the elements that need to be created :");
    private AnyList           anyMoreAnyN;
    private JLabel 			  lbl11;
    
    private JLabel            q106;
    private JRadioButton      q106_yes;
    private JRadioButton      q106_no;
    private JLabel            lblAnyMoreL        = new JLabel(
                                                       "<html><br />Please enter the names of the elements that need to be created :");
    private AnyList           anyMoreAnyL;
    private JLabel 			  lbl12;
    
    private int qNum = 1;

    /**
     * Create the panel.
     */
    public NetworkQuestions() {
        RowSpec[] rowSpec = new RowSpec[120];
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

        addNetworkQuestions();

    }

    /**
     * Construct network questions
     */
    private void addNetworkQuestions() {
        /*
         * 
         * 
         */
        lbl1 = new JLabel("<html><br />Network Structure :<br />");
        lbl1.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        // lbl1.setVisible(false);
        this.add(lbl1, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q2 = new JLabel("<html><br />"+String.valueOf(qNum++)+". How is the network structure defined");
        this.add(q2, getCoords(CCType.newQ));
        
        q2_nodeL = new JRadioButton("By describing the nodes location");
        q2_nodeL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q2_nodeL.isSelected()) {
                    q2_linkL.setSelected(!q2_nodeL.isSelected());
                    q2_both.setSelected(!q2_nodeL.isSelected());
                    q4.setVisible(true);
                    q4_euclid.setVisible(true);
                    q4_gps.setVisible(true);
                    q4_other.setVisible(true);
                    q7.setVisible(false);
                    q7_yes.setVisible(false);
                    q7_no.setVisible(false);
                    q8.setVisible(false);
                    q8_yes.setVisible(false);
                    q8_no.setVisible(false);
                    q100.setVisible(false);
                    q100_cost.setVisible(false);
                    q100_length.setVisible(false);
                    q100_time.setVisible(false);
                    q12.setVisible(false);
                    q12_value.setVisible(false);
                    q12_normal.setVisible(false);
                    q12_poisson.setVisible(false);
                    q12_other.setVisible(false);
                    lbl2.setVisible(true);
                    lbl3.setVisible(false);
                    q14.setEnabled(true);
                    q14_yes.setEnabled(true);
                    q14_no.setEnabled(true);
                    q15.setEnabled(true);
                    q15_yes.setEnabled(true);
                    q15_no.setEnabled(true);
                    q101.setVisible(true);
                    q101_yes.setVisible(true);
                    q101_no.setVisible(true);
                    lbl11.setVisible(true);
                    q105.setVisible(true);
                    q105_yes.setVisible(true);
                    q105_no.setVisible(true);
                    lbl12.setVisible(false);
                    q106.setVisible(false);
                    q106_yes.setVisible(false);
                    q106_no.setVisible(false);
                    lblAnyMoreL.setVisible(false);
                    anyMoreAnyL.setVisible(false);
                    q100_time.setSelected(false);
                    lblTimeAny.setVisible(false);
                    timeAny.setVisible(false);
                    timeAny.getListModel().clear();
                    q12_other.setSelected(false);
                } else {
                    q2_nodeL.setSelected(true);
                }
            }
        });
        this.add(q2_nodeL, getCoords(CCType.QToOption));

        q2_linkL = new JRadioButton("By describing the link lengths (distance matrix)");
        q2_linkL.setSelected(false);
        q2_linkL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q2_linkL.isSelected()) {
                    q2_nodeL.setSelected(!q2_linkL.isSelected());
                    q2_both.setSelected(!q2_linkL.isSelected());
                    q4.setVisible(false);
                    q4_euclid.setVisible(false);
                    q4_gps.setVisible(false);
                    q4_other.setVisible(false);
                    q7.setVisible(true);
                    q7_yes.setVisible(true);
                    q7_no.setVisible(true);
                    q8.setVisible(true);
                    q8_yes.setVisible(true);
                    q8_no.setVisible(true);
                    q100.setVisible(true);
                    q100_cost.setVisible(true);
                    q100_length.setVisible(true);
                    q100_time.setVisible(true);
                    lbl2.setVisible(false);
                    lbl3.setVisible(true);
                    q14.setEnabled(false);
                    q14_yes.setEnabled(false);
                    q14_no.setEnabled(false);
                    q15.setEnabled(false);
                    q15_yes.setEnabled(false);
                    q15_no.setEnabled(false);
                    q101.setVisible(false);
                    q101_yes.setVisible(false);
                    q101_no.setVisible(false);
                    lbl11.setVisible(false);
                    q105.setVisible(false);
                    q105_yes.setVisible(false);
                    q105_no.setVisible(false);
                    lblAnyMoreN.setVisible(false);
                    anyMoreAnyN.setVisible(false);
                    lbl12.setVisible(true);
                    q106.setVisible(true);
                    q106_yes.setVisible(true);
                    q106_no.setVisible(true);
                    lblLocationAny.setVisible(false);
                    locationAny.setVisible(false);
                } else {
                    q2_linkL.setSelected(true);
                }
            }
        });
        this.add(q2_linkL, getCoords(CCType.OptionToOption));

        q2_both = new JRadioButton("Both");
        q2_both.setSelected(false);
        q2_both.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q2_both.isSelected()) {

                    q2_nodeL.setSelected(!q2_both.isSelected());
                    q2_linkL.setSelected(!q2_both.isSelected());
                    q4.setVisible(true);
                    q4_euclid.setVisible(true);
                    q4_gps.setVisible(true);
                    q4_other.setVisible(true);
                    q7.setVisible(true);
                    q7_yes.setVisible(true);
                    q7_no.setVisible(true);
                    q8.setVisible(true);
                    q8_yes.setVisible(true);
                    q8_no.setVisible(true);
                    q100.setVisible(true);
                    q100_cost.setVisible(true);
                    q100_length.setVisible(true);
                    q100_time.setVisible(true);
                    lbl2.setVisible(true);
                    lbl3.setVisible(true);
                    q14.setEnabled(false);
                    q14_yes.setEnabled(false);
                    q14_no.setEnabled(false);
                    q15.setEnabled(false);
                    q15_yes.setEnabled(false);
                    q15_no.setEnabled(false);
                    q101.setVisible(true);
                    q101_yes.setVisible(true);
                    q101_no.setVisible(true);
                    lbl11.setVisible(true);
                    q105.setVisible(true);
                    q105_yes.setVisible(true);
                    q105_no.setVisible(true);
                    lbl12.setVisible(true);
                    q106.setVisible(true);
                    q106_yes.setVisible(true);
                    q106_no.setVisible(true);
                } else {
                    q2_both.setSelected(true);
                }
            }
        });
        this.add(q2_both, getCoords(CCType.OptionToOption));
        /*
         * 
         * 
         */
        lbl2 = new JLabel("<html><br /><br />Nodes :<br />");
        lbl2.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        lbl2.setVisible(false);
        this.add(lbl2, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q4 = new JLabel("<html><br />"+String.valueOf(qNum++)+". How are the node locations defined?");
        q4.setVisible(false);
        this.add(q4, getCoords(CCType.newQ));

        q4_euclid = new JCheckBox("Euclidean Coordinates (xy or xyz)");
        q4_euclid.setVisible(false);
        q4_euclid.setSelected(true);
        this.add(q4_euclid, getCoords(CCType.QToOption));

        q4_gps = new JCheckBox("GPS Coordinates");
        q4_gps.setVisible(false);
        this.add(q4_gps, getCoords(CCType.OptionToOption));

        q4_other = new JCheckBox("Other");
        q4_other.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q4_other.isSelected()) {
                    lblLocationAny.setVisible(true);
                    locationAny.setVisible(true);
                } else {
                    lblLocationAny.setVisible(false);
                    locationAny.setVisible(false);
                }
            }
        });
        q4_other.setVisible(false);
        this.add(q4_other, getCoords(CCType.OptionToOption));
        /*
         * 
         */
        lblLocationAny.setVisible(false);
        this.add(lblLocationAny, getCoords(CCType.newQ));
        /*
         * 
         */
        locationAny = new AnyList();
        locationAny.setVisible(false);
        this.add(locationAny, getCoords(CCType.newQ));
        /*
         * 
         */
        q101 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Are there several different types of nodes (e.g. customers, satelite depots, etc.)?");
        q101.setVisible(false);
        this.add(q101, getCoords(CCType.newQ));

        q101_yes = new JRadioButton("Yes");
        q101_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q101_yes.isSelected()) {
                    q101_no.setSelected(!q101_yes.isSelected());
                } else {
                    q101_yes.setSelected(true);
                }
            }
        });
        q101_yes.setVisible(false);
        this.add(q101_yes, getCoords(CCType.QToYes));

        q101_no = new JRadioButton("No");
        q101_no.setSelected(false);
        q101_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q101_no.isSelected()) {
                    q101_yes.setSelected(!q101_no.isSelected());
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
         * 
         */
        lbl11 = new JLabel("<html><br />Extra Node Constraints :<br />");
        lbl11.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        lbl11.setVisible(false);
        this.add(lbl11, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q105 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Do the nodes require any more constraints to be properly represented?");
        q105.setVisible(false);
        this.add(q105, getCoords(CCType.newQ));

        q105_yes = new JRadioButton("Yes");
        q105_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q105_yes.isSelected()) {
                    q105_no.setSelected(!q105_yes.isSelected());
                    lblAnyMoreN.setVisible(true);
                    anyMoreAnyN.setVisible(true);
                } else {
                    q105_yes.setSelected(true);
                }
            }
        });
        q105_yes.setVisible(false);
        this.add(q105_yes, getCoords(CCType.QToYes));

        q105_no = new JRadioButton("No");
        q105_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q105_no.isSelected()) {
                    q105_yes.setSelected(!q105_no.isSelected());
                    lblAnyMoreN.setVisible(false);
                    anyMoreAnyN.setVisible(false);
                } else {
                    q105_no.setSelected(true);
                }
            }
        });
        q105_no.setVisible(false);
        q105_no.setSelected(true);
        this.add(q105_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        lblAnyMoreN.setVisible(false);
        lblAnyMoreN.setVisible(false);
        this.add(lblAnyMoreN, getCoords(CCType.newQ));
        /*
         * 
         */
        anyMoreAnyN = new AnyList();
        anyMoreAnyN.setVisible(false);
        this.add(anyMoreAnyN, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        lbl3 = new JLabel("<html><br /><br />Links :<br />");
        lbl3.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        // lbl3.setHorizontalAlignment(JLabel.CENTER);
        lbl3.setVisible(false);
        this.add(lbl3, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q7 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Are there several different types of links (e.g. small and large roads)?");
        q7.setVisible(false);
        this.add(q7, getCoords(CCType.newQ));

        q7_yes = new JRadioButton("Yes");
        q7_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q7_yes.isSelected()) {
                    q7_no.setSelected(!q7_yes.isSelected());
                } else {
                    q7_yes.setSelected(true);
                }
            }
        });
        q7_yes.setVisible(false);
        this.add(q7_yes, getCoords(CCType.QToYes));

        q7_no = new JRadioButton("No");
        q7_no.setSelected(false);
        q7_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q7_no.isSelected()) {
                    q7_yes.setSelected(!q7_no.isSelected());
                } else {
                    q7_no.setSelected(true);
                }
            }
        });
        q7_no.setSelected(true);
        q7_no.setVisible(false);
        this.add(q7_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q8 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Are any of the links directed (i.e. have different values depending on the direction taken)?");
        q8.setVisible(false);
        this.add(q8, getCoords(CCType.newQ));

        q8_yes = new JRadioButton("Yes");
        q8_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q8_yes.isSelected()) {
                    q8_no.setSelected(!q8_yes.isSelected());
                } else {
                    q8_yes.setSelected(true);
                }
            }
        });
        q8_yes.setVisible(false);
        this.add(q8_yes, getCoords(CCType.QToYes));

        q8_no = new JRadioButton("No");
        q8_no.setSelected(false);
        q8_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q8_no.isSelected()) {
                    q8_yes.setSelected(!q8_no.isSelected());
                } else {
                    q8_no.setSelected(true);
                }
            }
        });
        q8_no.setSelected(true);
        q8_no.setVisible(false);
        this.add(q8_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q100 = new JLabel("<html><br />"+String.valueOf(qNum++)+". What weights do the links have");
        q100.setVisible(false);
        this.add(q100, getCoords(CCType.newQ));

        q100_cost = new JCheckBox("Cost (e.g. travel cost)");
        q100_cost.setVisible(false);
        this.add(q100_cost, getCoords(CCType.QToOption));

        q100_length = new JCheckBox("Length");
        q100_length.setVisible(false);
        this.add(q100_length, getCoords(CCType.OptionToOption));

        q100_time = new JCheckBox("Time (e.g. travel time)");
        q100_time.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q100_time.isSelected()) {
                    q12.setVisible(true);
                    q12_value.setVisible(true);
                    q12_normal.setVisible(true);
                    q12_poisson.setVisible(true);
                    q12_other.setVisible(true);
                } else {
                    q12.setVisible(false);
                    q12_value.setVisible(false);
                    q12_normal.setVisible(false);
                    q12_poisson.setVisible(false);
                    q12_other.setVisible(false);
                    q12_value.setSelected(true);
                    q12_normal.setSelected(false);
                    q12_poisson.setSelected(false);
                    q12_other.setSelected(false);
                    lblTimeAny.setVisible(false);
                    timeAny.setVisible(false);
                    timeAny.getListModel().clear();
                }
            }
        });
        
        q100_time.setVisible(false);
        this.add(q100_time, getCoords(CCType.OptionToOption));
        /*
         * 
         */
        q12 = new JLabel("<html><br />"+String.valueOf(qNum++)+". What type of time values need to be defined?");
        q12.setVisible(false);
        this.add(q12, getCoords(CCType.newQ));

        q12_value = new JCheckBox("Integer or Double Value");
        q12_value.setSelected(true);
        q12_value.setVisible(false);
        this.add(q12_value, getCoords(CCType.QToOption));

        q12_poisson = new JCheckBox("A time that follows a Normal distribution law");
        q12_poisson.setVisible(false);
        this.add(q12_poisson, getCoords(CCType.OptionToOption));

        q12_normal = new JCheckBox("A time that follows a Poisson distribution law");
        q12_normal.setVisible(false);
        this.add(q12_normal, getCoords(CCType.OptionToOption));

        q12_other = new JCheckBox("Another type of time value");
        q12_other.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q12_other.isSelected()) {
                    lblTimeAny.setVisible(true);
                    timeAny.setVisible(true);
                } else {
                    lblTimeAny.setVisible(false);
                    timeAny.setVisible(false);
                }
            }
        });
        q12_other.setVisible(false);
        this.add(q12_other, getCoords(CCType.OptionToOption));
        /*
         * 
         */
        lblTimeAny.setVisible(false);
        this.add(lblTimeAny, getCoords(CCType.newQ));
        /*
         * 
         */
        timeAny = new AnyList();
        timeAny.setVisible(false);
        this.add(timeAny, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        lbl12 = new JLabel("<html><br />Extra Link Constraints :<br />");
        lbl12.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        lbl12.setVisible(false);
        this.add(lbl12, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q106 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Do the links require any more constraints to be properly represented?");
        q106.setVisible(false);
        this.add(q106, getCoords(CCType.newQ));

        q106_yes = new JRadioButton("Yes");
        q106_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q106_yes.isSelected()) {
                    q106_no.setSelected(!q106_yes.isSelected());
                    lblAnyMoreL.setVisible(true);
                    anyMoreAnyL.setVisible(true);
                } else {
                    q106_yes.setSelected(true);
                }
            }
        });
        q106_yes.setVisible(false);
        this.add(q106_yes, getCoords(CCType.QToYes));

        q106_no = new JRadioButton("No");
        q106_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q106_no.isSelected()) {
                    q106_yes.setSelected(!q106_no.isSelected());
                    lblAnyMoreL.setVisible(false);
                    anyMoreAnyL.setVisible(false);
                } else {
                    q106_no.setSelected(true);
                }
            }
        });
        q106_no.setVisible(false);
        q106_no.setSelected(true);
        this.add(q106_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        lblAnyMoreL.setVisible(false);
        lblAnyMoreL.setVisible(false);
        this.add(lblAnyMoreL, getCoords(CCType.newQ));
        /*
         * 
         */
        anyMoreAnyL = new AnyList();
        anyMoreAnyL.setVisible(false);
        this.add(anyMoreAnyL, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        lbl4 = new JLabel("<html><br /><br />Further information describing the network :<br />");
        lbl4.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        this.add(lbl4, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q13 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Is the network complete (i.e. undirected graph where all node pairs are linked via a unique arc)?");
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
        this.add(q13_yes, getCoords(CCType.QToYes));

        q13_no = new JRadioButton("No");
        q13_no.setSelected(false);
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
        q13_no.setSelected(true);
        this.add(q13_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q14 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Is there a distance measurement that should be defined(e.g. euclidean, manhattan or geodesic)?");
        this.add(q14, getCoords(CCType.newQ));

        q14_yes = new JRadioButton("Yes");
        q14_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q14_yes.isSelected()) {
                    q14_no.setSelected(!q14_yes.isSelected());
                    lblDistMesure.setVisible(true);
                    textFieldDistMesure.setVisible(true);
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
                    lblDistMesure.setVisible(false);
                    textFieldDistMesure.setVisible(false);
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
        lblDistMesure = new JLabel(
                "<html><br />Please enter the distance measurement that should be used: ");
        lblDistMesure.setVisible(false);
        this.add(lblDistMesure, getCoords(CCType.newQ));
        /*
         * 
         */
        textFieldDistMesure = new JTextField();
        textFieldDistMesure.setVisible(false);
        this.add(textFieldDistMesure, getCoords(CCType.newQ));
        /*
         * 
         */
        q15 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Is there a rouding rule that should be defined(e.g. next highest integer)?");
        this.add(q15, getCoords(CCType.newQ));

        q15_yes = new JRadioButton("Yes");
        q15_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q15_yes.isSelected()) {
                    q15_no.setSelected(!q15_yes.isSelected());
                    lblRoundingRule.setVisible(true);
                    textFieldRoundingRule.setVisible(true);
                } else {
                    q15_yes.setSelected(true);
                }
            }
        });
        this.add(q15_yes, getCoords(CCType.QToYes));

        q15_no = new JRadioButton("No");
        q15_no.setSelected(false);
        q15_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q15_no.isSelected()) {
                    q15_yes.setSelected(!q15_no.isSelected());
                    lblRoundingRule.setVisible(false);
                    textFieldRoundingRule.setVisible(false);
                } else {
                    q15_no.setSelected(true);
                }
            }
        });
        q15_no.setSelected(true);
        this.add(q15_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        lblRoundingRule = new JLabel(
                "<html><br />Please enter the rounding rule that should be used :");
        lblRoundingRule.setVisible(false);
        this.add(lblRoundingRule, getCoords(CCType.newQ));
        /*
         * 
         */
        textFieldRoundingRule = new JTextField();
        textFieldRoundingRule.setVisible(false);
        this.add(textFieldRoundingRule, getCoords(CCType.newQ));
        /*
         * 
         */
        q16 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Is there any extra information that needs to be added in order to describe the network?");
        this.add(q16, getCoords(CCType.newQ));

        q16_yes = new JRadioButton("Yes");
        q16_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q16_yes.isSelected()) {
                    q16_no.setSelected(!q16_yes.isSelected());
                    lblDescripAny.setVisible(true);
                    descriptorAny.setVisible(true);
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
                    lblDescripAny.setVisible(false);
                    descriptorAny.setVisible(false);
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
        lblDescripAny.setVisible(false);
        this.add(lblDescripAny, getCoords(CCType.newQ));
        /*
         * 
         */
        descriptorAny = new AnyList();
        descriptorAny.setVisible(false);
        this.add(descriptorAny, getCoords(CCType.newQ));
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
     * Return boolean values of location types
     * 
     * @return table containing values
     */
    public boolean[] getLocationTypes() {
        boolean[] values = { q4_euclid.isSelected(), q4_gps.isSelected(), q4_other.isSelected() };
        return values;
    }

    /**
     * Return boolean values of time types
     * 
     * @return table containing values
     */
    public boolean[] getTimeTypes() {
        boolean[] values = { q12_value.isSelected(), q12_poisson.isSelected(),
                q12_normal.isSelected(), q12_other.isSelected() };
        return values;
    }

    /**
     * Getter for the variable q3_yes
     * 
     * @return the q3_yes
     */
    public boolean getHasLocation() {
        return (q2_nodeL.isSelected() || q2_both.isSelected());
    }

    /**
     * Getter for the variable q11_yes
     * 
     * @return the q11_yes
     */
    public boolean getHasTime() {
        return q100_time.isSelected();
    }

    /**
     * Getter for the variable q2_nodeL
     * 
     * @return the q2_nodeL
     */
    public JRadioButton getQ2_nodeL() {
        return q2_nodeL;
    }

    /**
     * Getter for the variable q2_linkL
     * 
     * @return the q2_linkL
     */
    public JRadioButton getQ2_linkL() {
        return q2_linkL;
    }

    /**
     * Getter for the variable q2_both
     * 
     * @return the q2_both
     */
    public JRadioButton getQ2_both() {
        return q2_both;
    }

    /**
     * Getter for the variable q4_euclid
     * 
     * @return the q4_euclid
     */
    public JCheckBox getQ4_euclid() {
        return q4_euclid;
    }

    /**
     * Getter for the variable q4_gps
     * 
     * @return the q4_gps
     */
    public JCheckBox getQ4_gps() {
        return q4_gps;
    }

    /**
     * Getter for the variable q4_other
     * 
     * @return the q4_other
     */
    public JCheckBox getQ4_other() {
        return q4_other;
    }

    /**
     * Getter for the variable q100_cost
     * 
     * @return the q100_cost
     */
    public JCheckBox getQ100_cost() {
        return q100_cost;
    }

    /**
     * Getter for the variable q100_length
     * 
     * @return the q100_length
     */
    public JCheckBox getQ100_length() {
        return q100_length;
    }

    /**
     * Getter for the variable q100_time
     * 
     * @return the q100_time
     */
    public JCheckBox getQ100_time() {
        return q100_time;
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
     * Getter for the variable q7_no
     * 
     * @return the q7_no
     */
    public JRadioButton getQ7_no() {
        return q7_no;
    }

    /**
     * Getter for the variable q8_yes
     * 
     * @return the q8_yes
     */
    public JRadioButton getQ8_yes() {
        return q8_yes;
    }

    /**
     * Getter for the variable q8_no
     * 
     * @return the q8_no
     */
    public JRadioButton getQ8_no() {
        return q8_no;
    }

    /**
     * Getter for the variable q12_value
     * 
     * @return the q12_value
     */
    public JCheckBox getQ12_value() {
        return q12_value;
    }

    /**
     * Getter for the variable q12_normal
     * 
     * @return the q12_normal
     */
    public JCheckBox getQ12_normal() {
        return q12_normal;
    }

    /**
     * Getter for the variable q12_poisson
     * 
     * @return the q12_poisson
     */
    public JCheckBox getQ12_poisson() {
        return q12_poisson;
    }

    /**
     * Getter for the variable q12_other
     * 
     * @return the q12_other
     */
    public JCheckBox getQ12_other() {
        return q12_other;
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
     * Getter for the variable timeAny
     * 
     * @return the timeAny
     */
    public AnyList getTimeAny() {
        return timeAny;
    }

    /**
     * Getter for the variable descriptorAny
     * 
     * @return the descriptorAny
     */
    public AnyList getDescriptorAny() {
        return descriptorAny;
    }

    /**
     * Getter for the variable locationAny
     * 
     * @return the locationAny
     */
    public AnyList getLocationAny() {
        return locationAny;
    }

    /**
     * Getter for the variable textFieldDistMesure
     * 
     * @return the textFieldDistMesure
     */
    public JTextField getTextFieldDistMesure() {
        return textFieldDistMesure;
    }

    /**
     * Getter for the variable textFieldRoundingRule
     * 
     * @return the textFieldRoundingRule
     */
    public JTextField getTextFieldRoundingRule() {
        return textFieldRoundingRule;
    }
    
    

    /**
	 * Getter for the variable q101_yes
	 * @return the q101_yes
	 */
	public JRadioButton getQ101_yes() {
		return q101_yes;
	}
	
	

	/**
	 * Getter for the variable q105_yes
	 * @return the q105_yes
	 */
	public JRadioButton getQ105_yes() {
		return q105_yes;
	}

	/**
	 * Getter for the variable anyMoreAnyN
	 * @return the anyMoreAnyN
	 */
	public AnyList getAnyMoreAnyN() {
		return anyMoreAnyN;
	}

	/**
	 * Getter for the variable q106_yes
	 * @return the q106_yes
	 */
	public JRadioButton getQ106_yes() {
		return q106_yes;
	}

	/**
	 * Getter for the variable anyMoreAnyL
	 * @return the anyMoreAnyL
	 */
	public AnyList getAnyMoreAnyL() {
		return anyMoreAnyL;
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
