
inc :: [Int] -> [Int]
inc [] = []
inc (n:ns) = n+1 : inc(ns)

sqr :: [Int] -> [Int]
sqr [] = []
sqr (n:ns) = n*n : sqr(ns)

inc' = map (+1)
sqr' = map (^2)

data Tree a = Leaf a | Node (Tree a) (Tree a)
    deriving Show

instance Functor Tree where
    -- fmap :: (a -> b) -> Tree a -> Tree b
    fmap g (Leaf x) = Leaf (g x)
    fmap g (Node l r) = Node (fmap g l) (fmap   g r)

inc'' :: Functor f => f Int -> f Int
inc'' = fmap (+1)

prods :: [Int] -> [Int] -> [Int]
prods xs ys = [ x * y | x <- xs, y <- ys ]

prods' :: [Int] -> [Int] -> [Int]
prods' xs ys = pure (*) <*> xs <*> ys

data Expr = Val Int | Div Expr Expr

safediv :: Int -> Int -> Maybe Int
safediv _ 0 = Nothing
safediv n m = Just (n `div` m) 

eval :: Expr -> Maybe Int
eval (Val n) = Just n
-- eval (Div x y) = eval x >>= \n -> eval y >>= \m -> safediv n m
eval (Div x y) = do n <- eval x
                    m <- eval y
                    safediv n m

pairs :: [a] -> [b] -> [(a, b)]
pairs xs ys = do x <- xs; y <- ys; return (x, y)

-- State Transformer

type State = Int
newtype ST a = S (State -> (a, State))
app :: ST a -> State -> (a, State)
app (S st) x = st x

instance Functor ST where
    -- fmap (a -> b) -> ST a -> ST b
    -- http://stackoverflow.com/questions/8274650/in-haskell-when-do-we-use-in-with-let
    fmap g st = S(\s -> let (x, s') = app st s in (g x, s'))

instance Applicative ST where
    -- pure :: a -> ST a
    pure x = S (\s -> (x, s))
    -- (<*>) ::ST (a -> b) -> ST a -> ST b
    stf <*> stx = S(\s -> let (f, s') = app stf s
                              (x, s'') = app stx s' in (f x, s''))

instance Monad ST where
    -- (>>=) :: ST a -> (a -> ST b) -> ST b
    st >>= f = S(\s -> let (x, s') = app st s in app (f x) s')

tree :: Tree Char
tree = Node (Node (Leaf 'a') (Leaf 'b')) (Leaf 'c')

fresh :: ST Int
fresh = S(\n -> (n, n + 1))

rlabel :: Tree a -> Int -> (Tree Int, Int)
rlabel (Leaf _)   n = ((Leaf n), n+1)
rlabel (Node l r) n = (Node l' r', n'')
                        where
                          (l', n') = rlabel r n
                          (r', n'') = rlabel l n'

alabel :: Tree a -> ST (Tree Int)
alabel (Leaf _) = Leaf <$> fresh
alabel (Node l r) = Node <$> alabel l <*> alabel r

mlabel :: Tree a -> ST (Tree Int)
mlabel (Leaf _) = do n <- fresh; return (Leaf n)
mlabel (Node l r) = do l' <- mlabel l
                       r' <- mlabel r
                       return (Node l' r')

data Tree' a = Leaf' a | Node' (Tree' a) a (Tree' a)
    deriving Show

instance Functor Tree' where
    -- fmap (a -> b) -> Tree' a -> Tree' b
    fmap g (Leaf' x) = Leaf' (g x)
    fmap g (Node' l x r) = Node' l' x' r'
                            where
                              l' = fmap g l
                              r' = fmap g r
                              x' = g x

tree' = Node' (Leaf' 1) 2 (Leaf' 3)

-- instance Functor ((->) a) where
--    -- fmap :: (b -> c) -> (a -> b) -> (a -> c)
--    fmap = (.)