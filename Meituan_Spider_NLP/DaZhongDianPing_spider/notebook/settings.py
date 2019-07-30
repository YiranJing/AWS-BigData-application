# -*- coding: utf-8 -*-

'''
meituan api spider's global variables settings.
'''


# HTTP Request headers infos setting variables:
headers = [
    {
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36',
        'Host': 'www.meituan.com',
        'Connection': 'keep-alive'
    }
]


# limit setting variable used to limit numbers of restaurants in one request:
limit = 25


