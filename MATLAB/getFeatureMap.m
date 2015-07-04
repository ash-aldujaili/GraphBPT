function theta = getFeatureMap(im, nodes)
% GETFEATUREMAP builds a feature map vector for the bpt represented by NODES built for IM
% the feature map represent the singleton saliency cues for each of the node plus the pairwise saliency cues
% For a N-BPT with D singleton saliency cues and M pairwilse saliency cues, THETA will be N*S + (N-1)*P
% Currently, S = 4 , P=1. THETA is later used to build the joint feature map, PHI, using GETJOINTMAP 
% which is nothing but a zero-padded version of THETA.


%1. Reshape the image for easy indexing by the nodes:
[r,c,d] = size(im);
im = single(reshape(im, [ r*c d ]));
S = 5; % singleton cues dimension / node
P = 0; % pairwise cues dimension / node-pair
N = length(nodes); % configuration assuming N is odd
%2. Get the singleton saliency cues:
singletonCues = zeros(S,N);

% backgroundness
for i = 1: N
  [x,y]=ind2sub([r,c],nodes(i).pixelsId);
  isBorder = sum(x == 1)+ sum(x == r) + sum(y == 1) + sum(y == c);
  singletonCues(1,i) = 1 - isBorder / numel(nodes(i).pixelsId);
  
end

% uniqueness & level cue
X = cov(im);
[V,D] = eig(X + 0.00001 * eye(size(X)));
D = diag(D);
D = D./sum(D);
V = bsxfun(@rdivide, V, D');
% get the PCA distance:
%pcaDist = abs(bsxfun(@minus,img,mean(img))*V(:,3));
pcaDistRGB = sum(abs(bsxfun(@minus,im,mean(im,1))*V(:,1:3)),2);
pcaDistRGB = mat2gray(pcaDistRGB);
for i = 1: N
  singletonCues(2,i) =  mean(pcaDistRGB(nodes(i).pixelsId));
  singletonCues(5,i) = nodes(i).level;
end

% contrast & uniformity with respect to the leaf nodes
meanRGB = cell(N,1);
stdRGB = cell(N,1);
nodesIdx = zeros(N,1);


meanRGB{1} = mean(im(nodes(1).pixelsId,:),1);
stdRGB{1} = std(im(nodes(1).pixelsId,:),1,1);
nodesIdx(1) = 1;
for i = 2:N
  meanRGB{i} = mean(im(nodes(i).pixelsId,:),1);
  stdRGB{i}  = std(im(nodes(i).pixelsId,:),1,1);
  nodesIdx(nodes(i).id+1) = i;
end

% start computing contrast and uniformity with a selected set of pseudobackgorund nodes
%[~,idx] = sort(singletonCues(1,:),'descend');
numLeafNodes = 7;
leafNodesStartIdx = floor(N/2);
leafNodesIdx = randperm(leafNodesStartIdx+1,numLeafNodes);
leafNodesIdx = leafNodesIdx + leafNodesStartIdx;

meanRGBref = cat(1, meanRGB{leafNodesIdx});
stdRGBref = cat(1, stdRGB{leafNodesIdx});

for i = 2:N
  singletonCues(3,i) = sum(pdist2(meanRGB{i}, meanRGBref));
  singletonCues(4,i) = sum(pdist2(stdRGB{i}, stdRGBref));
end

singletonCues(2,:) = mat2gray(singletonCues(2,:));
singletonCues(3,:) = mat2gray(singletonCues(3,:));
singletonCues(4,:) = mat2gray(singletonCues(4,:));
singletonCues(5,:) = mat2gray(singletonCues(5,:));
%3. Get the pairwise saliency cues:
% these are not essentially cues, but n-1 entries contain the index of parent nodes to the last n-1 node according to the order in
% nodes.

pairwiseCues = nodesIdx(cat(1,nodes(2:end).parentNode)+1);


%4. Produce THETA
theta = [ singletonCues(:); pairwiseCues(:)];

end

