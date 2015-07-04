% a function to show the features of the nodes
% im : the image
% l : the level of the nodes to be displayed
% f : which feature
% nodes : the structure that holds the nodes
% nodesFeature : array that holds the nodes features featuresxnodes

function showNodesFeatures(M,N,l,f,nodes,nodesFeature)

% choose the nodes at a certain level l:
idx = find(cat(1,nodes.level) == l);
nodes = nodes(idx);
nodesFeature = nodesFeature(idx,f);

% initialize the viewing map
out = zeros(M*N,1);
% go over the nodes of that level
for i = 1 : length(nodes)
  out(nodes(i).pixelsId,:) = nodesFeature(i);
end
% reshape and view:
out = (reshape(out, [M N]));
imshow(out,[ ]);


end
