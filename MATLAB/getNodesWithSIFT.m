% READLEAFPARENTNODES2 this function reads the files
% This function reads the parents.nodes and leaves.nodes for a given image
% leaves.nodes of a columns of integers arranged as the following for  each leaf node (line per node)
% [ node ID | node level | 1-D indices of all the dense sift features extracted by vl_phow whose centres are within the node region ]
% parents.nodes : columns of integers arranged as the following for each parent node (line per node)
%[ node ID | node level | ID of the immediate childrent in descending order | the Max ID of the subnodes beneath that node]
% This function must be provided with the mask path of the classes in the image, as well as the frames of the features to know the location
% this function is different from getNodes as it returns the nodes (both parent n leaves ) and thier attributes as a cellarray 
function [nodes] = getNodesWithSIFT(varargin)


% set parameters:
opts.leavesFP = './data/bpt.leaves';
opts.parentsFP = './data/bpt.parents';
opts.maxClass = 7;
opts.frames = [];
opts.maskPath = [];
opts = vl_argparse(opts, varargin);


if (isempty(opts.frames))
  error('frames from vl_phow() should be provided as an input');
end
% this is currently assuming each image has a mask of classes that can be indexed
if (isempty(opts.maskPath))
 error('A the mask path for the image should be provided. For example, voc07 has masks in SegmentationClass directory');
end
% read the class mask:
classMask = imread(opts.maskPath);
% get the image size:
[M,N] = size(classMask);

%%%%%%%%%%%
% LEAVES:
%%%%%%%%%%%


% read the leaf nodes file:
fileID = fopen(opts.leavesFP);
% read each line as a string
S = textscan(fileID,'%s','Delimiter','\n'); % returns a single cell of cell array
S = S{1};
% get separate each entry/line
% each cell has the first element as the matlab node id followed by the descriptors 1d index as in vl_phow descr (assuming the parameters used by both bpt and vl_phow are the same)
L = cellfun(@(x) textscan(x,'%d'), S, 'UniformOutput', 1);

leafNodes = cellfun(@(x) getLeafAttributes(x, classMask, opts.frames, M, N), L, 'UniformOutput',1);
%%%%%%%%%%%%
% PARENTS:
%%%%%%%%%%%%

P = load('-ascii', opts.parentsFP);
P = mat2cell(P, ones(size(P,1),1), 5);

% some required info from the leaves:
leafNodesId = cat(1, leafNodes(:).id);
parentNodes = cellfun(@(x) getParentAttributes(x, leafNodesId, leafNodes), P, 'UniformOutput',1);


% concatenate all the nodes
nodes = cat(1, leafNodes, parentNodes);
end

%------------------------------------------------------
% Function getLeafAttributes this function reads the attributes of each node from the leaves file, class mask and frames and
% return the attributes as a struct
function leafAttributes = getLeafAttributes(x, classMask, frames, M, N, maxClass)


loc = (sub2ind([M N], frames(2, x(3:end)), frames(1, x(3:end)))) ;
descrsClass = classMask(loc);
descrsClass(descrsClass == 255) = DONTCARE;
leafAttributes = struct('id', x(1), 'level', x(2), 'descrsId', x(3:end), 'descrsLoc', loc, 'descrsClass', descrsClass, 'class',[], 'childNodes',[]);
leafAttributes.class = getNodeClass(descrsClass, x(2));
leafAttributes.classHist = hist(descrsClass, 0 : maxClass+1);
end
%-----------------------------------------------------
%------------------------------------------------------
% Function getParentAttributes this function reads the attributes of each node from the parents file, class mask and frames and
% return the attributes as a struct
function parentAttributes = getParentAttributes(x, leafNodesId, leafNodes, maxClass)

DONTCARE = 22;
[~,~,idx] = intersect(x(4):x(5), leafNodesId);

descrsId = cat(1,leafNodes(idx).descrsId);
descrsLoc = cat(2,leafNodes(idx).descrsLoc);
descrsClass = cat(2,leafNodes(idx).descrsClass);
parentAttributes = struct('id', x(1), 'level', x(2), 'descrsId', descrsId, 'descrsLoc', descrsLoc, 'descrsClass', descrsClass, 'class',[], 'childNodes', x(3:4));
parentAttributes.class = getNodeClass(descrsClass, x(2));
parentAttributes.classHist = hist(descrsClass, 0:maxClass + 1 );

end

% %-----------------------------------------------------
% % this function decides what should be the optimum class of each node 
% % currently it could be multiple,background, irrelevant, or any of the 20 classes of PASCAL VOC data
% % it takes as an input a set of representative points from the node
% % this can be extended to multiscale labelling by considering the levels
% function nodeClass = getNodeClass(nodePtsClasses, level)
% 
% 
% % check if the node has no descriptors sampled from its pixels
% if (length(nodePtsClasses) == 0)
%   error('empty node')
% end
% 
% MULTIPLE = 21;
% DONTCARE = 22;
% % object threshold
% OBJ_TH = 0.001 * level * level ; 
% MULT_TH = 0.9;%1.0 / (1 + 0.01 * level);
% % check if the node has an object class:
% bgClass = nodePtsClasses(nodePtsClasses == 0);
% objectClasses = nodePtsClasses(nodePtsClasses ~= 0);
% objectClassesFiltered = objectClasses(objectClasses ~= DONTCARE);
% 
% % default node class
% nodeClass = 0;
% objectsClasses = unique(objectClassesFiltered);
% numObjects = length(objectsClasses);
% 
% 
% % another way of addressing which object
% if (numObjects == 1)
%   nodeClass = objectsClasses;
% elseif (numObjects > 1)
%   nodeClass = [MULTIPLE objectsClasses];
% end
% %if ~isempty(objectClassesFiltered)
%   % check how many objects:
% %  numObjects = length(unique(objectClassesFiltered));
% %  dominantClass = mode(objectClassesFiltered);
% %  if ( (numel(objectClasses) / numel(nodePtsClasses)) > OBJ_TH) 
%     % check if the objects are dominated by one 
%     %if ((numel(objectClassesFiltered(objectClassesFiltered == dominantClass)) / numel(objectClassesFiltered)) > MULT_TH)
% %    if (numObjects == 1 || (numel(objectClassesFiltered(objectClassesFiltered == dominantClass)) / numel(objectClassesFiltered)) > MULT_TH);
% %      nodeClass = dominantClass;
% %    else
% %      nodeClass = 22;
% %    end
% % end
% %end
% 
% end

function nodeClass = getNodeClass(nodePtsClasses,maxClass)


% check if the node has no descriptors sampled from its pixels
if (isempty(nodePtsClasses))
  error('empty node')
end


% object threshold
MULTIPLE = maxClass + 1;
OBJ_TH = 0.8 ; 
%MULT_TH = 0.9;%1.0 / (1 + 0.01 * level);

% check if the node has an object class:
%bgClass = nodePtsClasses(nodePtsClasses == 0);
%objectClasses = nodePtsClasses(nodePtsClasses ~= 0);
%objectClassesFiltered = objectClasses(objectClasses ~= DONTCARE);

% default node class
objectsClasses = unique(nodePtsClasses);
numObjects = length(objectsClasses);
dominantClass = mode(nodePtsClasses);

nodeClass = dominantClass;
% another way of addressing which object
if (numObjects > 1)
    if (sum(nodePtsClasses(:) == dominantClasss)/numel(nodePtsClasses(:)) < OBJ_TH)
        nodeClass = MULTIPLE;
    end
end

end

