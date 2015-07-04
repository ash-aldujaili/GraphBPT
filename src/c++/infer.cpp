/*  This file was adapted from libDAI's example.cpp and Daphne Koller's doinference.cpp to perform   
 *  inference on the binary partition tree by Abdullah Al-Dujaili.
 *
 *  Copyright (c) 2006-2011, The libDAI authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license that can be found in the LICENSE file.
 */


#include <iostream>
#include <map>
#include <dai/alldai.h> // include main libDAI header file
#include <dai/jtree.h> // using only junction tree algorithm
#include <dai/bp.h> // using only belief propogation

using namespace dai;
using namespace std;

// do infer with Jt:
void inferJT(FactorGraph fg, bool isMAP)
{
	// set some properties:
	PropertySet opts;
	opts.set("updates",string("HUGIN")); // update method
	if(isMAP)
		opts.set("inference",string("MAXPROD"));
	// Build the jtree
	JTree jt = JTree(fg, opts);	
	jt.init();
	jt.run();

	if(isMAP) // read the MAP inference results (y for max p(y|x,w):
	{
		vector<size_t> mapstate = jt.findMaximum();
		// Report MAP Joint state
		cerr << "Exact MAP state (log score = " << fg.logScore(mapstate) << "):"<< endl;
		cout << fg.nrVars() << endl;
		for(size_t i = 0; i < mapstate.size(); i++)
			cout << fg.var(i).label() << " " << mapstate[i] << endl;
	} 
	else // read the probabilistic inference results (y_i for max p(y_i|x,w)
	{
		cerr << "Exact Highest Posterior state" << endl;	
		cout << fg.nrVars() << endl;		
		for(size_t i = 0; i < fg.nrVars(); i++)
		{
			Factor marginal = jt.belief(fg.var(i));
			Real maxprob = marginal.max();
			for (size_t j = 0; j < marginal.nrStates(); j++)
			{
				if(marginal[j] == maxprob)
				{
					cout<< fg.var(i).label() << " " << j <<endl;
					j = marginal.nrStates(); // report only one state
				}
			}
		}
	
	}
	// bbye !
	return;
}

// do infer with BP:
void inferBP(FactorGraph fg, bool isMAP)
{
	// Set some constants
        size_t maxiter = 10000;
        Real   tol = 1e-9;
        size_t verb = 1;

        // Store the constants in a PropertySet object
        PropertySet opts;
        opts.set("maxiter",maxiter);  // Maximum number of iterations
        opts.set("tol",tol);          // Tolerance for convergence
        opts.set("verbose",verb);     // Verbosity (amount of output generated)
	opts.set("updates",string("SEQRND"));
	opts.set("logdomain",false); // update method
	if(isMAP)
	{
		opts.set("inference",string("MAXPROD"));
		opts.set("damping",string("0.1"));
	}	
	// build the belief propogtaion structure:
	BP bp = BP(fg, opts);	
	bp.init();
	bp.run();

	if(isMAP) // read the MAP inference results (y for max p(y|x,w):
	{
		vector<size_t> mapstate = bp.findMaximum();
		// Report MAP Joint state
		cerr << "MAP state (log score = " << fg.logScore(mapstate) << "):"<< endl;
		cout << fg.nrVars() << endl;
		for(size_t i = 0; i < mapstate.size(); i++)
			cout << fg.var(i).label() << " " << mapstate[i] << endl;
	} 
	else // read the probabilistic inference results (y_i for max p(y_i|x,w)
	{
		cerr << "Highest Posterior state" << endl;
		cout << fg.nrVars() << endl;			
		for(size_t i = 0; i < fg.nrVars(); i++)
		{
			Factor marginal = bp.belief(fg.var(i));
			Real maxprob = marginal.max();
			for (size_t j = 0; j < marginal.nrStates(); j++)
			{
				if(marginal[j] == maxprob)
				{
					cout<< fg.var(i).label() << " " << j <<endl;
					j = marginal.nrStates(); // report only one state
				}
			}
		}
	
	}
	// bbye !
	return;
}


int main(int argc, char* argv[]) {
	if(argc != 4)
	{
		cout << "Usage: " << argv[0] << " <filename.fg> [map|pif] [jt|bp]" << endl << endl;
		cout << "Reads factor graph <filename.fg> and runs" << endl;
		cout << "JunctionTree & Belief propogation on it."<< endl;
		cout << "map: finds the maximum a posteriori state" << endl;
		cout << "pif: computes the marginals of each node " << endl;
		cout << "jt: to perform the inference using junction tree" << endl;
		cout << "bp: to perform the inference using belief propogation" << endl;
		return 1;
	} 
	else 
	{

		// check the flag correctness
		bool isMAP = false;
		if(strcmp(argv[2], "map") == 0){
			isMAP = true;
		}	
		else if(strcmp(argv[2], "pif") != 0)
		{
			cout << "Usage: " << argv[0] << " <filename.fg> [map|pif] [jt|bp]" << endl << endl;
			cout << argv[2] << " is not among the valid inference tasks {map,pif}" << endl;	
			return 1;
		}
		bool isBP = false;
		if(strcmp(argv[3], "bp") == 0){
			isBP = true;
		}
		else if(strcmp(argv[3], "jt") != 0)
		{
			cout << "Usage: " << argv[0] << " <filename.fg> [map|pif] [jt|bp]" << endl << endl;
			cout << argv[3] << " is not among the valid inference methods {jt,bp}" << endl;	
			return 1;

		}

		// redirect the error stream to ("inference.log")
        	ofstream errlog("inference.log");
		streambuf *cerrbuf = cerr.rdbuf(); //save old buf
        	cerr.rdbuf(errlog.rdbuf());

		// Read FactorGraph from the file specified by the first command line argument:
		FactorGraph fg;
		fg.ReadFromFile(argv[1]);
	

		// infer !:		
		if (isBP)
		{
			inferBP(fg, isMAP);
		}
		else
		{
			inferJT(fg, isMAP);
		}

		// redirect the error stream again:
		cerr.rdbuf(cerrbuf);
		// close the stream
		errlog.close();


		// bbye !
		return 0;
	}
}



