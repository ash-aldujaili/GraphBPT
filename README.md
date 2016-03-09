#GraphBPT 

Welcome to :sparkles: GraphBPT :sparkles:, this software has been developed in part of the publication :

Abdullah Al-Dujaili, François Merciol and Sébastien Lefèvre. GraphBPT: An Efficient Hierarchical Data Structure for Image Representation and Probabilistic Inference (ISMM) may 2015, Iceland  accessible [here](http://link.springer.com/chapter/10.1007%2F978-3-319-18720-4_26#page-1)

In _/bin_, there are two jars, 
   IsmBpt2015.jar : GUI-like demo, it is also hosted [here](https://www-obelix.irisa.fr/software/)
   
   bpt.jar : can be used in MATLAB as shown below, as well as to run the segmentation example in the paper.

# Demo Example
~~~
java -jar bin/IsmmBpt2015.jar [–help] [-D dataDir] [image]
~~~
# MATLAB Example
From MATLAB directory, run matlab:
~~~
% read an image
imgName = '../1.jpg'
img = imread(imgName);
[M,N,C] = size(img);
% build the tree
img = single(reshape(img,[M*N C]));
system(['java -cp ../bin/bpt.jar BinaryPartitionTree ' imgName]);
% get the nodes
nodes = getNodesWithPixels('isNoClass', true);
% show a region
showNodeRegion(img,M,N,C,nodes(end-1));
% build some features and show them 
nodesFeautres = getNodesFeatures(nodes,img,M,N);
showNodesFeatures(M,N,1,1,nodes,nodesFeautres);
~~~
# Graphical Model Segmentation Example

I have configured the tool to do the segmentation on the example image, I am assuming here you are having linux-64, as the inference engine is compiled for that.
anyway, I have put the inference source file along, but you need to compile it with its depenedencies of libdai inference library (refer to the paper)
~~~
java -cp bin/bpt.jar TreeGraph [image]
~~~
 
 ## Citation
 
If you write a scientific paper describing research that made use of this code, please cite the following paper:

~~~
@InBook{Al-Dujaili2015,
  Title                    = {Mathematical Morphology and Its Applications to Signal and Image Processing: 12th International Symposium, ISMM 2015, Reykjavik, Iceland, May 27-29, 2015. Proceedings},
  Author                   = {Al-Dujaili, Abdullah
and Merciol, Fran{\c{c}}ois
and Lef{\`e}vre, S{\'e}bastien},
  Chapter                  = {GraphBPT: An Efficient Hierarchical Data Structure for Image Representation and Probabilistic Inference},
  Editor                   = {Benediktsson, Atli J{\'o}n
and Chanussot, Jocelyn
and Najman, Laurent
and Talbot, Hugues},
  Pages                    = {301--312},
  Publisher                = {Springer International Publishing},
  Year                     = {2015},
  Address                  = {Cham},
  Doi                      = {10.1007/978-3-319-18720-4_26},
  ISBN                     = {978-3-319-18720-4},
  Url                      = {http://dx.doi.org/10.1007/978-3-319-18720-4_26}
}
~~~
  
Abdullah Al-Dujaili
