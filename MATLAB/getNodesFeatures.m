% a function to get node features
function nodesFeatures = getNodeFeatures(nodes,img,M,N)
% a simple example to compute the features of each node, here a node is assigned a boolean whether it is a boundary or not

% initialize 
nodesFeatures = zeros(length(nodes),1);

% 1. Is it a boundary pixel
for i = 1: length(nodes)
  [x,y]=ind2sub([M,N],nodes(i).pixelsId);
  isBorder = sum(x == 1)+ sum(x == M) + sum(y == 1) + sum(y == N);
  nodesFeatures(i,1) = 1 - isBorder / numel(nodes(i).pixelsId);
end
end
