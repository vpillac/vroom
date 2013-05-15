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
 * Contains all questions relevant to the requests
 * 
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a> 
 */
public class RequestQuestions extends JPanel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2708572501394226283L;

    private int               xCoord           = 2;
    private int               yCoord           = 4;

    private JLabel            q1;
    private JRadioButton      q1_node;
    private JRadioButton      q1_link;
    private JRadioButton      q1_both;
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
    private JCheckBox         q6_start;
    private JCheckBox         q6_end;
    private JLabel            q7;
    private JRadioButton      q7_yes;
    private JRadioButton      q7_no;
    private JLabel            q8;
    private JRadioButton      q8_yes;
    private JRadioButton      q8_no;
    private JLabel            q9;
    private JRadioButton      q9_yes;
    private JRadioButton      q9_no;
    private JLabel            q10;
    private JRadioButton      q10_yes;
    private JRadioButton      q10_no;
    private JLabel            q11;
    private JCheckBox         q11_value;
    private JCheckBox         q11_normal;
    private JCheckBox         q11_poisson;
    private JCheckBox         q11_other;
    private JLabel            lblDemandAny     = new JLabel(
                                                       "<html><br />Please enter the names of the elements that need to be created :");
    private AnyList           demandAny;
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
    private JCheckBox         q16_value;
    private JCheckBox         q16_normal;
    private JCheckBox         q16_poisson;
    private JCheckBox         q16_other;
    private JLabel            lblTimeAny       = new JLabel(
                                                       "<html><br />Please enter the names of the elements that need to be created :");
    private AnyList           timeAny;
    private JLabel            q17;
    private JRadioButton      q17_yes;
    private JRadioButton      q17_no;
    private JLabel            q18;
    private JCheckBox         q18_prec;
    private JCheckBox         q18_succ;
    private JLabel            q19;
    private JRadioButton      q19_yes;
    private JRadioButton      q19_no;
    private JLabel            q20;
    private JRadioButton      q20_yes;
    private JRadioButton      q20_no;
    private JLabel            q100;
    private JRadioButton      q100_yes;
    private JRadioButton      q100_no;

    private JLabel            lbl1;
    private JLabel            lbl2;
    private JLabel            lbl3;
    private JLabel            lbl4;
    private JLabel            lbl5;
    
    private JLabel            q105;
    private JRadioButton      q105_yes;
    private JRadioButton      q105_no;
    private JLabel            lblAnyMore        = new JLabel(
                                                       "<html><br />Please enter the names of the elements that need to be created :");
    private AnyList           anyMoreAny;
    private JLabel 			  lbl11;
    
    private int qNum = 1;

    /**
     * Create the panel.
     */
    public RequestQuestions() {
        RowSpec[] rowSpec = new RowSpec[188];
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

        addRequestQuestions();

    }

    /**
     * Construct request questions
     */
    private void addRequestQuestions() {
        /*
         * 
         * 
         */
        lbl1 = new JLabel("<html><br />Request Characteristics :<br />");
        lbl1.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        // lbl1.setVisible(false);
        this.add(lbl1, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q1 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". What are the requests attached to?");
        this.add(q1, getCoords(CCType.newQ));

        q1_node = new JRadioButton("Nodes");
        q1_node.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q1_node.isSelected()) {
                    q1_link.setSelected(!q1_node.isSelected());
                    q1_both.setSelected(!q1_node.isSelected());
                } else {
                    q1_node.setSelected(true);
                }
            }
        });
        q1_node.setSelected(true);
        this.add(q1_node, getCoords(CCType.QToOption));

        q1_link = new JRadioButton("Links (Arcs)");
        q1_link.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q1_link.isSelected()) {
                    q1_node.setSelected(!q1_link.isSelected());
                    q1_both.setSelected(!q1_link.isSelected());
                } else {
                    q1_link.setSelected(true);
                }
            }
        });
        this.add(q1_link, getCoords(CCType.OptionToOption));

        q1_both = new JRadioButton("Both");
        q1_both.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q1_both.isSelected()) {
                    q1_node.setSelected(!q1_both.isSelected());
                    q1_link.setSelected(!q1_both.isSelected());
                } else {
                    q1_both.setSelected(true);
                }
            }
        });
        this.add(q1_both, getCoords(CCType.OptionToOption));
        /*
         * 
         */
        q3 = new JLabel(
                "<html><br/>"+String.valueOf(qNum++)+". Do several different types of requests need to be defined (e.g. pickup and delivery)?");
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
        q12 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Do any of the requests receive prizes on completion?");
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
        q12_no.setSelected(true);
        this.add(q12_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q13 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Do any of the requests have servicing costs?");
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
        q19 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Do any of the requests require certain skills for completion?");
        this.add(q19, getCoords(CCType.newQ));

        q19_yes = new JRadioButton("Yes");
        q19_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q19_yes.isSelected()) {
                    q19_no.setSelected(!q19_yes.isSelected());
                } else {
                    q19_yes.setSelected(true);
                }
            }
        });
        this.add(q19_yes, getCoords(CCType.QToYes));

        q19_no = new JRadioButton("No");
        q19_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q19_no.isSelected()) {
                    q19_yes.setSelected(!q19_no.isSelected());
                } else {
                    q19_no.setSelected(true);
                }
            }
        });
        q19_no.setSelected(true);
        this.add(q19_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q20 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Do any of the requests require certain tools for completion?");
        this.add(q20, getCoords(CCType.newQ));

        q20_yes = new JRadioButton("Yes");
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
        this.add(q20_yes, getCoords(CCType.QToYes));

        q20_no = new JRadioButton("No");
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
        q20_no.setSelected(true);
        this.add(q20_no, getCoords(CCType.YesToNo));
        /*
         * 
         * 
         */
        lbl4 = new JLabel("<html><br />Service Time :<br />");
        lbl4.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        // lbl4.setVisible(false);
        this.add(lbl4, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q15 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Do any of the requests have service times?");
        this.add(q15, getCoords(CCType.newQ));

        q15_yes = new JRadioButton("Yes");
        q15_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q15_yes.isSelected()) {
                    q15_no.setSelected(!q15_yes.isSelected());
                    q16.setVisible(true);
                    q16_value.setVisible(true);
                    q16_normal.setVisible(true);
                    q16_poisson.setVisible(true);
                    q16_other.setVisible(true);
                } else {
                    q15_yes.setSelected(true);
                }
            }
        });
        this.add(q15_yes, getCoords(CCType.QToYes));

        q15_no = new JRadioButton("No");
        q15_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q15_no.isSelected()) {
                    q15_yes.setSelected(!q15_no.isSelected());
                    q16.setVisible(false);
                    q16_value.setVisible(false);
                    q16_normal.setVisible(false);
                    q16_poisson.setVisible(false);
                    q16_other.setVisible(false);
                    q16_value.setSelected(true);
                    q16_normal.setSelected(false);
                    q16_poisson.setSelected(false);
                    q16_other.setSelected(false);
                    lblTimeAny.setVisible(false);
                    timeAny.setVisible(false);
                    timeAny.getListModel().clear();
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
        q16 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". What type of service times do the requests have?");
        q16.setVisible(false);
        this.add(q16, getCoords(CCType.newQ));

        q16_value = new JCheckBox("Integer or Double Value");
        q16_value.setVisible(false);
        q16_value.setSelected(true);
        this.add(q16_value, getCoords(CCType.QToOption));

        q16_normal = new JCheckBox("Time that follows a Normal distribution law");
        q16_normal.setVisible(false);
        this.add(q16_normal, getCoords(CCType.OptionToOption));

        q16_poisson = new JCheckBox("Time that follows a Poisson distribution law");
        q16_poisson.setVisible(false);
        this.add(q16_poisson, getCoords(CCType.OptionToOption));

        q16_other = new JCheckBox("Another type of time");
        q16_other.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q16_other.isSelected()) {
                    lblTimeAny.setVisible(true);
                    timeAny.setVisible(true);
                } else {
                    lblTimeAny.setVisible(false);
                    timeAny.setVisible(false);
                }
            }
        });
        q16_other.setVisible(false);
        this.add(q16_other, getCoords(CCType.OptionToOption));
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
        lbl2 = new JLabel("<html><br />Time Windows :<br />");
        lbl2.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        this.add(lbl2, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q4 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Should the requests be satisfied within certain time windows?");
        this.add(q4, getCoords(CCType.newQ));

        q4_yes = new JRadioButton("Yes");
        q4_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q4_yes.isSelected()) {
                    q4_no.setSelected(!q4_yes.isSelected());
                    q5.setVisible(true);
                    q5_yes.setVisible(true);
                    q5_no.setVisible(true);
                    q6.setVisible(true);
                    q6_start.setVisible(true);
                    q6_end.setVisible(true);
                    q7.setVisible(true);
                    q7_yes.setVisible(true);
                    q7_no.setVisible(true);
                    q8.setVisible(true);
                    q8_yes.setVisible(true);
                    q8_no.setVisible(true);
                } else {
                    q4_yes.setSelected(true);
                }
            }
        });
        this.add(q4_yes, getCoords(CCType.QToYes));

        q4_no = new JRadioButton("No");
        q4_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q4_no.isSelected()) {
                    q4_yes.setSelected(!q4_no.isSelected());
                    q5.setVisible(false);
                    q5_yes.setVisible(false);
                    q5_no.setVisible(false);
                    q6.setVisible(false);
                    q6_start.setVisible(false);
                    q6_end.setVisible(false);
                    q7.setVisible(false);
                    q7_yes.setVisible(false);
                    q7_no.setVisible(false);
                    q8.setVisible(false);
                    q8_yes.setVisible(false);
                    q8_no.setVisible(false);
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
        q5 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Could a request have several different time windows?");
        q5.setVisible(false);
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
        q5_yes.setVisible(false);
        this.add(q5_yes, getCoords(CCType.QToYes));

        q5_no = new JRadioButton("No");
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
        q5_no.setVisible(false);
        q5_no.setSelected(true);
        this.add(q5_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q6 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". What type of constraints do the time windows have?");
        q6.setVisible(false);
        this.add(q6, getCoords(CCType.newQ));

        q6_start = new JCheckBox("Start");
        q6_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q6_start.isSelected()) {
                    q7.setVisible(true);
                    q7_yes.setVisible(true);
                    q7_no.setVisible(true);
                } else {
                    q7.setVisible(false);
                    q7_yes.setVisible(false);
                    q7_no.setVisible(false);
                }
            }
        });
        q6_start.setVisible(false);
        q6_start.setSelected(true);
        this.add(q6_start, getCoords(CCType.QToOption));

        q6_end = new JCheckBox("End");
        q6_end.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q6_end.isSelected()) {
                    q8.setVisible(true);
                    q8_yes.setVisible(true);
                    q8_no.setVisible(true);
                } else {
                    q8.setVisible(false);
                    q8_yes.setVisible(false);
                    q8_no.setVisible(false);
                }
            }
        });
        q6_end.setVisible(false);
        q6_end.setSelected(true);
        this.add(q6_end, getCoords(CCType.OptionToOption));
        /*
         * 
         */
        q7 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Is the start time strict (i.e. is not variable)?");
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
        q7_yes.setSelected(true);
        q7_yes.setVisible(false);
        this.add(q7_yes, getCoords(CCType.QToYes));

        q7_no = new JRadioButton("No");
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
        q7_no.setVisible(false);
        this.add(q7_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q8 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Is the end time strict (i.e. is not variable)?");
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
        q8_yes.setSelected(true);
        q8_yes.setVisible(false);
        this.add(q8_yes, getCoords(CCType.QToYes));

        q8_no = new JRadioButton("No");
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
        q8_no.setVisible(false);
        this.add(q8_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q14 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Are there any dynamic requests that have release dates?");
        this.add(q14, getCoords(CCType.newQ));

        q14_yes = new JRadioButton("Yes");
        q14_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q14_yes.isSelected()) {
                    q14_no.setSelected(!q14_yes.isSelected());
                } else {
                    q14_yes.setSelected(true);
                }
            }
        });
        this.add(q14_yes, getCoords(CCType.QToYes));

        q14_no = new JRadioButton("No");
        q14_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q14_no.isSelected()) {
                    q14_yes.setSelected(!q14_no.isSelected());
                } else {
                    q14_no.setSelected(true);
                }
            }
        });
        q14_no.setSelected(true);
        this.add(q14_no, getCoords(CCType.YesToNo));
        /*
         * 
         * 
         */
        lbl3 = new JLabel("<html><br />Demand Quantities :<br />");
        lbl3.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        // lbl3.setVisible(false);
        this.add(lbl3, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q9 = new JLabel(
                "<html><br/>"+String.valueOf(qNum++)+". Do the requests have quantities to deliver (or pick up)?");
        this.add(q9, getCoords(CCType.newQ));

        q9_yes = new JRadioButton("Yes");
        q9_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q9_yes.isSelected()) {
                    q9_no.setSelected(!q9_yes.isSelected());
                    q10.setVisible(true);
                    q10_yes.setVisible(true);
                    q10_no.setVisible(true);
                    q11.setVisible(true);
                    q11_value.setVisible(true);
                    q11_normal.setVisible(true);
                    q11_poisson.setVisible(true);
                    q11_other.setVisible(true);
                    q100.setVisible(true);
                    q100_yes.setVisible(true);
                    q100_no.setVisible(true);
                } else {
                    q9_yes.setSelected(true);
                }
            }
        });
        this.add(q9_yes, getCoords(CCType.QToYes));

        q9_no = new JRadioButton("No");
        q9_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q9_no.isSelected()) {
                    q9_yes.setSelected(!q9_no.isSelected());
                    q10.setVisible(false);
                    q10_yes.setVisible(false);
                    q10_no.setVisible(false);
                    q11.setVisible(false);
                    q11_value.setVisible(false);
                    q11_normal.setVisible(false);
                    q11_poisson.setVisible(false);
                    q11_other.setVisible(false);
                    q11_value.setSelected(true);
                    q11_normal.setSelected(false);
                    q11_poisson.setSelected(false);
                    q11_other.setSelected(false);
                    q100.setVisible(false);
                    q100_yes.setVisible(false);
                    q100_no.setVisible(false);
                    q11_other.setSelected(false);
                    lblDemandAny.setVisible(false);
                    demandAny.setVisible(false);
                    demandAny.getListModel().clear();
                } else {
                    q9_no.setSelected(true);
                }
            }
        });
        q9_no.setSelected(true);
        this.add(q9_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q100 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Can the requests be serviced in more than one visit?");
        q100.setVisible(false);
        this.add(q100, getCoords(CCType.newQ));

        q100_yes = new JRadioButton("Yes");
        q100_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q100_yes.isSelected()) {
                    q100_no.setSelected(!q100_yes.isSelected());
                } else {
                    q100_yes.setSelected(true);
                }
            }
        });
        q100_yes.setVisible(false);
        this.add(q100_yes, getCoords(CCType.QToYes));

        q100_no = new JRadioButton("No");
        q100_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q100_no.isSelected()) {
                    q100_yes.setSelected(!q100_no.isSelected());
                } else {
                    q100_no.setSelected(true);
                }
            }
        });
        q100_no.setSelected(true);
        q100_no.setVisible(false);
        this.add(q100_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q10 = new JLabel(
                "<html><br/>"+String.valueOf(qNum++)+". Could one request have multiple quantities to deliver (or pick up)?");
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
        q11 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". What type of quantities do the requests have?");
        q11.setVisible(false);
        this.add(q11, getCoords(CCType.newQ));

        q11_value = new JCheckBox("Integer or Double Value");
        q11_value.setSelected(true);
        q11_value.setVisible(false);
        this.add(q11_value, getCoords(CCType.QToOption));

        q11_normal = new JCheckBox("Quantities that follows a Normal distribution law");
        q11_normal.setVisible(false);
        this.add(q11_normal, getCoords(CCType.OptionToOption));

        q11_poisson = new JCheckBox("Quantities that follows a Poisson distribution law");
        q11_poisson.setVisible(false);
        this.add(q11_poisson, getCoords(CCType.OptionToOption));

        q11_other = new JCheckBox("Another type of quantity");
        q11_other.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q11_other.isSelected()) {
                    lblDemandAny.setVisible(true);
                    demandAny.setVisible(true);
                } else {
                    lblDemandAny.setVisible(false);
                    demandAny.setVisible(false);
                }
            }
        });
        q11_other.setVisible(false);
        this.add(q11_other, getCoords(CCType.OptionToOption));
        /*
         * 
         */
        lblDemandAny.setVisible(false);
        this.add(lblDemandAny, getCoords(CCType.newQ));
        /*
         * 
         */
        demandAny = new AnyList();
        demandAny.setVisible(false);
        this.add(demandAny, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        lbl5 = new JLabel("<html><br />Request Dependencies :<br />");
        lbl5.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        // lbl5.setVisible(false);
        this.add(lbl5, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q17 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". Are there any dependencies between requests");
        this.add(q17, getCoords(CCType.newQ));

        q17_yes = new JRadioButton("Yes");
        q17_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q17_yes.isSelected()) {
                    q17_no.setSelected(!q17_yes.isSelected());
                    q18.setVisible(true);
                    q18_prec.setVisible(true);
                    q18_succ.setVisible(true);
                } else {
                    q17_yes.setSelected(true);
                }
            }
        });
        this.add(q17_yes, getCoords(CCType.QToYes));

        q17_no = new JRadioButton("No");
        q17_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (q17_no.isSelected()) {
                    q17_yes.setSelected(!q17_no.isSelected());
                    q18.setVisible(false);
                    q18_prec.setVisible(false);
                    q18_succ.setVisible(false);
                } else {
                    q17_no.setSelected(true);
                }
            }
        });
        q17_no.setSelected(true);
        this.add(q17_no, getCoords(CCType.YesToNo));
        /*
         * 
         */
        q18 = new JLabel("<html><br/>"+String.valueOf(qNum++)+". How must the dependencies be defined?");
        q18.setVisible(false);
        this.add(q18, getCoords(CCType.newQ));

        q18_prec = new JCheckBox("List all the preceding requests for each request");
        q18_prec.setVisible(false);
        q18_prec.setSelected(true);
        this.add(q18_prec, getCoords(CCType.QToOption));

        q18_succ = new JCheckBox("List all the succeeding requests for each request");
        q18_succ.setVisible(false);
        this.add(q18_succ, getCoords(CCType.OptionToOption));
        /*
         * 
         * 
         */
        lbl11 = new JLabel("<html><br />Extra Request Constraints :<br />");
        lbl11.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        this.add(lbl11, getCoords(CCType.newQ));
        /*
         * 
         * 
         */
        q105 = new JLabel(
                "<html><br />"+String.valueOf(qNum++)+". Do the requests require any more constraints to be properly represented?");
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
     * Return boolean values of demand types
     * 
     * @return table containing values
     */
    public boolean[] getDemandTypes() {
        boolean[] values = { q11_value.isSelected(), q11_poisson.isSelected(),
                q11_normal.isSelected(), q11_other.isSelected() };
        return values;
    }

    /**
     * Return boolean values of time types
     * 
     * @return table containing values
     */
    public boolean[] getTimeTypes() {
        boolean[] values = { q16_value.isSelected(), q16_poisson.isSelected(),
                q16_normal.isSelected(), q16_other.isSelected() };
        return values;
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
     * Getter for the variable q15_yes
     * 
     * @return the q15_yes
     */
    public JRadioButton getQ15_yes() {
        return q15_yes;
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
     * Getter for the variable q18_prec
     * 
     * @return the q18_prec
     */
    public JCheckBox getQ18_prec() {
        return q18_prec;
    }

    /**
     * Getter for the variable q18_succ
     * 
     * @return the q18_succ
     */
    public JCheckBox getQ18_succ() {
        return q18_succ;
    }

    /**
     * Getter for the variable q1_node
     * 
     * @return the q1_node
     */
    public JRadioButton getQ1_node() {
        return q1_node;
    }

    /**
     * Getter for the variable q1_link
     * 
     * @return the q1_link
     */
    public JRadioButton getQ1_link() {
        return q1_link;
    }

    /**
     * Getter for the variable q1_both
     * 
     * @return the q1_both
     */
    public JRadioButton getQ1_both() {
        return q1_both;
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
     * Getter for the variable q6_start
     * 
     * @return the q6_start
     */
    public JCheckBox getQ6_start() {
        return q6_start;
    }

    /**
     * Getter for the variable q6_end
     * 
     * @return the q6_end
     */
    public JCheckBox getQ6_end() {
        return q6_end;
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
     * Getter for the variable q11_value
     * 
     * @return the q11_value
     */
    public JCheckBox getQ11_value() {
        return q11_value;
    }

    /**
     * Getter for the variable q11_normal
     * 
     * @return the q11_normal
     */
    public JCheckBox getQ11_normal() {
        return q11_normal;
    }

    /**
     * Getter for the variable q11_poisson
     * 
     * @return the q11_poisson
     */
    public JCheckBox getQ11_poisson() {
        return q11_poisson;
    }

    /**
     * Getter for the variable q11_other
     * 
     * @return the q11_other
     */
    public JCheckBox getQ11_other() {
        return q11_other;
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
     * Getter for the variable q15_no
     * 
     * @return the q15_no
     */
    public JRadioButton getQ15_no() {
        return q15_no;
    }

    /**
     * Getter for the variable q16_value
     * 
     * @return the q16_value
     */
    public JCheckBox getQ16_value() {
        return q16_value;
    }

    /**
     * Getter for the variable q16_normal
     * 
     * @return the q16_normal
     */
    public JCheckBox getQ16_normal() {
        return q16_normal;
    }

    /**
     * Getter for the variable q16_poisson
     * 
     * @return the q16_poisson
     */
    public JCheckBox getQ16_poisson() {
        return q16_poisson;
    }

    /**
     * Getter for the variable q16_other
     * 
     * @return the q16_other
     */
    public JCheckBox getQ16_other() {
        return q16_other;
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
     * Getter for the variable q100_yes
     * 
     * @return the q100_yes
     */
    public JRadioButton getQ100_yes() {
        return q100_yes;
    }

    /**
     * Getter for the variable q100_no
     * 
     * @return the q100_no
     */
    public JRadioButton getQ100_no() {
        return q100_no;
    }

    /**
     * Getter for the variable demandAny
     * 
     * @return the demandAny
     */
    public AnyList getDemandAny() {
        return demandAny;
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
