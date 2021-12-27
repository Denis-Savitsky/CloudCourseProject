
-- file: E:\Tinkoff-scala\cloudProject\auth\src\main\scala\ru\hes\auth\db\DB.scala
-- line: 21
-- col: 14
-- time: 2021-12-05T16:47:18.4030385
SELECT cred.id, cred.login, cred.password FROM public.creds cred WHERE cred.login = ? LIMIT 1;


-- file: E:\Tinkoff-scala\cloudProject\auth\src\main\scala\ru\hes\auth\db\DB.scala
-- line: 34
-- col: 51
-- time: 2021-12-05T16:47:20.443105
INSERT INTO public.creds DEFAULT VALUES RETURNING id, login, password;

