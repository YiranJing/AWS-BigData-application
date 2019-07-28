#!/usr/bin/env python3
# -*- coding: utf-8 -*-

'''
Define a MeituanSpider class allows you to fetch meituan
restaurants infos in shanghai city.
'''

import csv
import json
import os
import requests
import pymysql
import random
import time
from pymongo import MongoClient
from py2neo import Graph, Node
from settings import headers,limit

# Data storage path and filename(.csv or .txt file) setting variables:
savePath = './meituanZhangYeyeInfos'
filename = 'ShangHaiRestaurants'


class MeituanSpider_shanghai(object):
    '''
    MeituanSpider class allows you to fetch all data from meituan api website.
    :Usage:
    '''
    ## 上海世纪汇店铺
    baseUrl_1 = ("https://www.meituan.com/meishi/api/poi/getMerchantComment?uuid=57461b13249c4026be0d.1564202509.1.0.0&platform=1&partner=126&originUrl=https%3A%2F%2Fwww.meituan.com%2Fmeishi%2F179108554%2F&riskLevel=1&optimusCode=10&id=179108554&userId=&offset=0&pageSize=10&sortType=1")
    ## 上海东方路店
    baseUrl_2 = ("https://www.meituan.com/meishi/api/poi/getMerchantComment?uuid=57461b13249c4026be0d.1564202509.1.0.0&platform=1&partner=126&originUrl=https%3A%2F%2Fwww.meituan.com%2Fmeishi%2F152551521%2F&riskLevel=1&optimusCode=10&id=152551521&userId=&offset=0&pageSize=10&sortType=1")
  
    #上海美团张爷爷空心面美食爬虫
    def __init__(self, saveMode='csv'):
        print("Spider is ready for shanghai area")

    def run(self):
        url = self.baseUrl_1
        taglist_1, itemlist_1 = self.parse(url)
        
        url = self.baseUrl_2
        taglist_2, itemlist_2 = self.parse(url)
        
        taglist = taglist_1 +taglist_2
        itemlist = itemlist_1 +itemlist_2
        return taglist, itemlist
        
    def parse(self,url):
        response = requests.get(url,headers=random.choice(headers))
        number = 0
        while True:
            try:
                info_dict = json.loads(response.text)
                info_list = info_dict['data'] 
                if info_list:
                    break
                else:
                    print('some wrong')
                    number += 1
                    if number >= 10:
                        return None
                    time.sleep(10)
                    response = requests.get(url, headers=random.choice(headers))
            except:
                number += 1
                if number >= 10:
                    return None
                time.sleep(10)
                response = requests.get(url, headers=random.choice(headers))
        
        ##用户总体评价   
        taglist = [] 
        try:
            for comment in info_list['tags']:
                count = comment['count']
                tag = comment['tag']
            
                tag_item = {
                    '标签':tag,
                    '累计数量':count
                }
                taglist.append(tag_item)
        except TypeError: # if no tags information
            taglist = [] 
        
        ##用户具体评价
        itemlist = []
        for comment in info_list['comments']:
            username = comment['userName']
            avgPrice = comment['avgPrice']
            user_comment = comment['comment']  
            commenttime = comment['commentTime']
            menu = comment['menu']
            dealEndtime = comment['dealEndtime']
            star = comment['star']
            
            item = {
                    '用户名': username,
                    '平均消费': avgPrice,
                    '星级': star,
                    '菜单': menu,
                    '评价时间': commenttime,
                    '用餐结束时间': dealEndtime,
                    '评价': user_comment
                }
            itemlist.append(item)       
        return taglist, itemlist


    def __del__(self):
        '''
        The deconstructor of MeituanSpider class.
        Deconstructs an instance of MeituanSpider, closes MongoDB database and
        files.
        '''
        print('>>>> Finish.')
            

# test:
if __name__ == '__main__':
    spider = MeituanSpider(saveMode='csv')
    spider.run()