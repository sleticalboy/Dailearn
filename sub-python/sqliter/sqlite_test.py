import sqlite3


def run_sqlite():
    conn = sqlite3.connect('sqler.db')
    cursor = conn.cursor()
    print(conn, cursor)
    cursor.execute('''create table if not exists user(
    _id integer primary key autoincrement default 0 ,
    _name varchar(32) not null ,
    _email varchar(32) not null ,
    _password varchar(32) not null ,
    _token text default null
    );''')
    for i in range(20):
        cursor.execute("insert into user (_name, _email, _password) values ('bin.li', 'bin.li@quvideo.com', 'hello')")
    conn.commit()
    cursor.execute('select * from user')
    res = cursor.fetchall()
    for r in res:
        print(r)
    cursor.execute('delete from user where _name=?', ['bin.li'])
    res = cursor.fetchall()
    print(res)
    cursor.close()
    conn.close()
    pass


if __name__ == '__main__':
    run_sqlite()
    pass
