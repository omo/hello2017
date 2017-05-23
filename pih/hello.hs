
double x = x + x
quadruple x = double (double x)
factorial n = product [1..n]
average ns = sum ns `div` length ns

-- Chapter 3 Exercises

bools :: [Bool]
bools = [True, False]

nums :: [[Int]]
nums = [[1,2], [3,4]]

add :: Int -> Int -> Int -> Int
add x y z = x + y + z

copy :: a -> (a, a)
copy x = (x, x)

apply :: (a -> b) -> a -> b
apply f x = f x

second :: [a] -> a
second xs = head (tail xs)

swap :: (a, b) -> (b, a)
swap (x, y) = (y, x)

pair :: a -> b -> (a, b)
pair x y = (x, y)

double' :: Num a => a -> a
double' x = x*2

palindrome :: Eq a => [a] -> Bool
palindrome xs = reverse xs == xs

twice :: (a -> a) -> a -> a
twice f x = f (f x)

-- Chapter 4 Excersises

halve :: [a] -> ([a], [a])
halve xs = (take n xs, drop n xs) where
            n = (length xs) `div` 2

third :: [a] -> a
third [_, _, x] = x
third xs = third (tail xs)

third' :: [a] -> a
third' xs = xs !! 2

mult :: Int -> Int -> Int -> Int
mult = (\x -> (\y -> (\z -> x*y*z)))

safetail :: [a] -> [a]
safetail [] = []
safetail xs = tail xs

-- Chapter 5 Excersises

grid :: Int -> Int -> [(Int, Int)]
grid x y = [(i, j) | i <- [0..x], j <- [0..y] ]

square :: Int -> [(Int, Int)]
square x = grid x x

replicate' :: Int -> a -> [a]
replicate' n x = [x | _ <- [1..n]]

pyth :: Int -> [(Int, Int, Int)]
pyth n = [(i, j, k) | i <- [1..n], j <- [1..n], k <- [1..n], i^2 + j^2 == k^2]

factors :: Int -> [Int]
factors n = [ x | x <- [1..n], n `mod` x == 0]

perfects :: Int -> [Int]
perfects n = [ x | x <- [1..n], sum (factors x) == x*2 ]

find :: Eq a => a -> [(a, b)] -> [b]
find x t = [ y | (x', y) <- t, x' == x ]

positions :: Eq a => a -> [a] -> [Int]
positions x xs = find x (zip xs [0..])

scalarproduct :: [Int] -> [Int] -> Int
scalarproduct xs ys = sum [ x*y | (x, y) <- zip xs ys ]

-- Chapter 6 Exercises

sumdown :: Int -> Int
sumdown 0 = 0
sumdown x = x + sumdown(x - 1)

exp' :: Int -> Int -> Int
exp' x 0 = 1
exp' x y = x * (exp' x (y - 1))

euclid :: Int -> Int -> Int
euclid x y | x == y    = x
           | x  < y    = euclid x (y - x)
           | otherwise = euclid y x

and' :: [Bool] -> Bool
and' [] = True
and' (x:xs) | x == True = (and' xs)
            | otherwise = False

concat' :: [[a]] -> [a]
concat' [] = []
concat' (xs:xss) = xs ++ (concat' xss)

replicate'' :: Int -> a -> [a]
replicate'' 0 _ = []
replicate'' n x = x : replicate'' (n - 1) x

nth :: [a] -> Int -> a
nth (x:_)  0 = x
nth (x:xs) n = nth xs (n - 1)

elem' :: Eq a => a -> [a] -> Bool
elem' _ [] = False
elem' x (y:ys) | x == y    = True
               | otherwise = elem' x ys

merge :: Ord a => [a] -> [a] -> [a]
merge [] xs = xs
merge xs [] = xs
merge (x:xs) (y:ys) | x < y     = x : (merge xs (y:ys))
                    | otherwise = y : (merge (x:xs) ys)

msort :: Ord a => [a] -> [a]
msort []  = []
msort [x] = [x]
msort xs = merge (msort ys) (msort zs) where
            (ys, zs) = halve xs
