% a function to show the node region from the image
% it requires the image contents as (M*N)X3 array, M, N and the node

function showNodeRegion(im,M,N,C,node)

im = reshape(im, [M*N C]);
out = zeros(M*N,C);
out(node.pixelsId,:) = im(node.pixelsId,:);
out = uint8(reshape(out, [M N C]));

imshow(out,[]);


end
