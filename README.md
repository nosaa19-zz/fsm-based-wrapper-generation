FSM-based Wrapper Generation Documentation

Overview
Generally, the goal of the wrapper generation for automatic data extraction is to build self-trained wrapper so that it can produce consistent output. We design two phases for our FSM-based Wrapper Generation. Firstly, the Training Phase where we generate the FSM from the main table and sub-tables of the unsupervised data extraction output i.e. DCADE. Secondly, the Testing Phase where we designed a universal FSM wrapper to verify whether a testing page (represented by a list of leaf nodes) complies with the page schema by aligning the leaf nodes of the input page with the page schema.
