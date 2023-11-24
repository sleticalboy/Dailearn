class PrjGenerator(object):

    def __init__(self, algo_type: str, algo_name: str, work_dir: str):
        self.algo_type = algo_type
        self.algo_name = algo_name
        self.work_dir = work_dir
        pass

    def gen_prj(self) -> (str, str):
        name = f"{self.work_dir}/algo-{self.algo_name}"
        if self.algo_type == 'python':
            name += '-py'
        return name + '.zip', "mock"
