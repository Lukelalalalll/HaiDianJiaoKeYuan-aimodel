from flask import Flask, request, jsonify, send_file
import subprocess
import os
import shutil
import uuid
import glob

app = Flask(__name__)

# 确保主工作目录存在
MAIN_WORKSPACE_DIR = './localworkspace'
if not os.path.exists(MAIN_WORKSPACE_DIR):
    os.makedirs(MAIN_WORKSPACE_DIR)

@app.route('/generate', methods=['POST'])
def ocr():
    # 获取上传的文件
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    # 生成唯一的随机目录
    random_dir = uuid.uuid4().hex
    WORKSPACE_DIR = os.path.join(MAIN_WORKSPACE_DIR, random_dir)
    RESULTS_DIR = os.path.join(WORKSPACE_DIR, 'results')
    PDFS_DIR = os.path.join(WORKSPACE_DIR, 'pdfs')

    # 确保工作目录存在
    os.makedirs(WORKSPACE_DIR, exist_ok=True)
    os.makedirs(RESULTS_DIR, exist_ok=True)
    os.makedirs(PDFS_DIR, exist_ok=True)

    # 保存文件到临时位置
    file_path = os.path.join(PDFS_DIR, file.filename)
    file.save(file_path)

    # 构建命令
    command = [
        'python', '-m', 'olmocr.pipeline',
        WORKSPACE_DIR,
        '--pdfs', file_path
    ]

    # 调用 olmocr.pipeline 命令
    try:
        result = subprocess.run(
            command,
            capture_output=True,
            text=True,
            check=True
        )
    except subprocess.CalledProcessError as e:
        return jsonify({"error": e.stderr}), 500

    # 查找生成的 .jsonl 文件
    jsonl_files = glob.glob(os.path.join(RESULTS_DIR, '*.jsonl'))
    if not jsonl_files:
        print("未找到生成的 .jsonl 文件")
        return jsonify({"error": "Result file not found"}), 500

    # 假设只有一个 .jsonl 文件
    result_file_path = jsonl_files[0]

    # 返回结果文件
    response = send_file(result_file_path, as_attachment=True)
    #return result_file_path
    return response

if __name__ == '__main__':
    # 指定端口
    app.run(debug=True, host='0.0.0.0', port=3002)
