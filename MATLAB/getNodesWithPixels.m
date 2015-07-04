% READLEAFPARENTNODES2 this function reads the files
% This function reads the parents.nodes and leaves.nodes for a given image
% leaves.nodes of a columns of integers arranged as the following for  each leaf node (line per node)
% [ node ID | node weight | node level | 1-D indices of all the dense sift features extracted by vl_phow whose centres are within the node region ]
% parents.nodes : columns of integers arranged as the following for each parent node (line per node)
%[ node ID | node weight | node level | ID of the immediate childrent in descending order | the Max ID of the subnodes beneath that node]
% This function must be provided with the mask path of the classes in the image, as well as the frames of the features to know the location
% this function is different from getNodes as it returns the nodes (both parent n leaves ) and thier attributes as a cellarray
% It needs vl_feat library. 
% example:
%   system(['java -cp bpt.jar BinaryPartitionTree ' imgName]);
%  % get the structure of the tree:
%  nodes = getNodesWithPixels('isNoClass', true);
% author Abdullah Al-Dujaili
function [nodes] = getNodesWithPixels(varargin)


% set parameters:
opts.leavesFP = './data/bpt.leavesPixels';
opts.parentsFP = './data/bpt.parentsPixels';
opts.isNoClass = true; % set it true if we are not doing assignment of nodes to classes
opts.maskPath = [];
opts.classMask= [];
% use vl_feat library if you want to have this special parsing
%opts = vl_argparse(opts, varargin);



% this is currently assuming each image has a mask of classes that can be indexed
if (isempty(opts.maskPath) && isempty(opts.classMask) && ~opts.isNoClass)
 error('A the mask path for the image should be provided. For example, voc07 has masks in SegmentationClass directory');
end
% read the class mask:
if (isempty(opts.classMask) && ~opts.isNoClass)
    classMask = mat2gray(imread(opts.maskPath));
    [m,n,c] = size(classMask);
    classMask = reshape(classMask,[ m*n 1]);
else
    classMask = opts.classMask;
end
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

leafNodes = cellfun(@(x) getLeafAttributes(x, classMask, opts.isNoClass), L, 'UniformOutput',1);
%%%%%%%%%%%%
% PARENTS:
%%%%%%%%%%%%

P = load('-ascii', opts.parentsFP);
P = mat2cell(P, ones(size(P,1),1), 7);

% some required info from the leaves:
leafNodesId = cat(1, leafNodes(:).id);
parentNodes = cellfun(@(x) getParentAttributes(x, leafNodesId, leafNodes, opts.isNoClass), P, 'UniformOutput',1);


% concatenate all the nodes
nodes = cat(1, leafNodes, parentNodes);
end

%------------------------------------------------------
% Function getLeafAttributes this function reads the attributes of each node from the leaves file, class mask and frames and
% return the attributes as a struct
function leafAttributes = getLeafAttributes(x, classMask, isNoClass)

if isNoClass
  leafAttributes = struct('id', x(1),'weight', x(2), 'level', x(3),  'parentNode', x(4),'pixelsId', x(5:end),'childNodes',[]);
else
  pixelsClass = classMask(x(5:end));
  leafAttributes = struct('id', x(1), 'weight', x(2),'level', x(3),  'parentNode', x(4),'pixelsId', x(5:end), 'pixelsClass', pixelsClass, 'class',[], 'childNodes',[]);
  leafAttributes.class = getNodeClass(pixelsClass);
  %leafAttributes.classHist = hist(single(pixelsClass), 0 : maxClass+1);
end
end
%-----------------------------------------------------
%------------------------------------------------------
% Function getParentAttributes this function reads the attributes of each node from the parents file, class mask and frames and
% return the attributes as a struct
function parentAttributes = getParentAttributes(x, leafNodesId, leafNodes,isNoClass)

[~,~,idx] = intersect(x(6):x(7), leafNodesId);
pixelsId = cat(1,leafNodes(idx).pixelsId);
if isNoClass
  parentAttributes = struct('id', x(1), 'weight', x(2), 'level', x(3), 'parentNode', x(4), 'pixelsId', pixelsId, 'childNodes', x(5:6));
else
  pixelsClass = cat(1,leafNodes(idx).pixelsClass);
  parentAttributes = struct('id', x(1), 'weight', x(2),'level', x(3), 'parentNode', x(4),'pixelsId', pixelsId,'pixelsClass', pixelsClass, 'class',[], 'childNodes', x(5:6));
  parentAttributes.class = getNodeClass(pixelsClass);
  %parentAttributes.classHist = hist(single(pixelsClass), 0:maxClass+1);
end

end

%-----------------------------------------------------
% this function decides what should be the optimum class of each node 
% currently it could be multiple,background, irrelevant, or any of the 6 classes of ISPRS data
% it takes as an input a set of representative points from the node
% this can be extended to multiscale labelling by considering the levels
function nodeClass = getNodeClass(nodePtsClasses)
nodeClass = round(mean(nodePtsClasses)*10)/10;

end



